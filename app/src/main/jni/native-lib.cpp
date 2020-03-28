
#include <jni.h>
#include "AIWine.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_potato_wuziqi_AI_getAiObject(JNIEnv *env, jobject instance) {
    AIWine* ai = new AIWine();
    ai->setSize(15);
    return (jlong )ai;
// TODO
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_potato_wuziqi_AI_getBestPoint(JNIEnv *env, jobject instance, jlong aiObject, jint move) {
    AIWine* ai=(AIWine*)aiObject;
    if(move>=0)
    {
        int x =move>>4;
        int y=move&15;
        ai->turnMove(x,y);
    }
    int bestX,bestY;
    ai->turnBest(bestX,bestY);
    return (bestX<<4)+bestY;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_potato_wuziqi_AI_aiRestart(JNIEnv *env, jobject instance, jlong aiObject) {
    AIWine* ai=(AIWine*)aiObject;
    ai->restart();
    // TODO
}

extern "C"
JNIEXPORT void JNICALL
Java_com_potato_wuziqi_AI_aiUndo(JNIEnv *env, jobject instance, jlong aiObject) {
    AIWine* ai=(AIWine*)aiObject;
    ai->turnUndo();
    ai->turnUndo();
    // TODO

}

extern "C"
JNIEXPORT void JNICALL
Java_com_potato_wuziqi_AI_setStepTime(JNIEnv *env, jobject instance, jlong aiObject, jint time) {
    AIWine* ai=(AIWine*)aiObject;
    ai->timeout_turn = time;
    // TODO

}
extern "C"
JNIEXPORT jint JNICALL
Java_com_potato_wuziqi_AI_getSuggest(JNIEnv *env, jobject instance, jlong aiObject) {
    AIWine* ai=(AIWine*)aiObject;
    int bestX,bestY;
    ai->turnBest(bestX,bestY);
    ai->turnUndo();
    return (bestX<<4)+bestY;
    // TODO

}
extern "C"
JNIEXPORT void JNICALL
Java_com_potato_wuziqi_AI_aiMove(JNIEnv *env, jobject instance, jlong aiObject, jint move) {
    AIWine* ai=(AIWine*)aiObject;
    int x =move>>4;
    int y=move&15;
    ai->turnMove(x,y);
    // TODO

}