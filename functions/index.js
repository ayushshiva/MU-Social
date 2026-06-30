"use strict";

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");
const { RtcRole, RtcTokenBuilder } = require("agora-access-token");

admin.initializeApp();

exports.generateAgoraToken = onCall({ region: "us-central1" }, (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Authentication is required.");
  }

  const channelName = request.data && request.data.channelName;
  if (typeof channelName !== "string" || channelName.trim() === "") {
    throw new HttpsError("invalid-argument", "A non-empty channelName is required.");
  }

  const appId = process.env.AGORA_APP_ID;
  const appCertificate = process.env.AGORA_APP_CERTIFICATE;
  if (!appId || !appCertificate) {
    throw new HttpsError("failed-precondition", "Agora token service is not configured.");
  }

  const role = request.data.role === 1 ? RtcRole.PUBLISHER : RtcRole.SUBSCRIBER;
  const expiresAt = Math.floor(Date.now() / 1000) + 3600;
  const token = RtcTokenBuilder.buildTokenWithUid(
    appId,
    appCertificate,
    channelName,
    0,
    role,
    expiresAt
  );

  return { token, expiresAt };
});

exports.sendLiveGift = onCall({ region: "us-central1" }, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Authentication is required.");
  }

  const senderId = request.auth.uid;
  const receiverId = request.data && request.data.receiverId;
  const streamId = request.data && request.data.streamId;
  const giftId = request.data && request.data.giftId;
  const giftName = request.data && request.data.giftName;
  const coinValue = Number(request.data && request.data.coinValue);

  if (![receiverId, streamId, giftId, giftName].every((value) => typeof value === "string" && value.trim() !== "")) {
    throw new HttpsError("invalid-argument", "Gift transaction data is incomplete.");
  }
  if (!Number.isInteger(coinValue) || coinValue <= 0) {
    throw new HttpsError("invalid-argument", "coinValue must be a positive integer.");
  }

  const db = admin.firestore();
  const senderRef = db.collection("wallets").doc(senderId);
  const receiverRef = db.collection("wallets").doc(receiverId);
  const txRef = db.collection("gift_transactions").doc();
  const liveGiftRef = db.collection("live_streams").doc(streamId).collection("live_gifts").doc(txRef.id);

  await db.runTransaction(async (transaction) => {
    const senderSnapshot = await transaction.get(senderRef);
    const senderWallet = senderSnapshot.data();
    if (!senderWallet) {
      throw new HttpsError("failed-precondition", "Sender wallet not found.");
    }
    if ((senderWallet.balance || 0) < coinValue) {
      throw new HttpsError("failed-precondition", "Insufficient balance.");
    }

    const giftTransaction = {
      id: txRef.id,
      streamId,
      senderId,
      receiverId,
      giftId,
      giftName,
      coinValue,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    };

    transaction.update(senderRef, {
      balance: admin.firestore.FieldValue.increment(-coinValue)
    });
    transaction.set(receiverRef, {
      userId: receiverId,
      totalEarned: admin.firestore.FieldValue.increment(coinValue)
    }, { merge: true });
    transaction.set(txRef, giftTransaction);
    transaction.set(liveGiftRef, giftTransaction);
  });

  return { success: true };
});
