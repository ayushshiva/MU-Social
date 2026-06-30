# MU Social - Crash Risk Areas

## 1. Out of Memory (OOM)
**File**: `PostRepositoryImpl.kt`
**Risk**: `createPost` takes `List<ByteArray>`. Large image lists (5+ high-res photos) can exceed JVM heap limits.

## 2. Null Auth States
**File**: `SocialRepositoryImpl.kt`
**Risk**: Repositories assume `auth.currentUser` is non-null. Triggering a profile update exactly during a logout will crash.

## 3. Firestore Transaction Conflicts
**File**: `PostRepositoryImpl.kt`
**Risk**: `likePost` uses a transaction. High-velocity likes on a trending post might cause repeated retries or failures if contention is high.

## 4. Fragment View Lifecycles
**File**: `HomeScreen.kt`
**Risk**: `LaunchedEffect` tracking post views might trigger on a stale ID if the LazyColumn recomposes too aggressively.