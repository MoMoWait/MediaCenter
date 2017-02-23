#define LOG_TAG "mediacenter-jni native.cpp"
#include <utils/Log.h>
#include <stdio.h>
#include "jni.h"
#include <media/IMediaPlayerService.h>
#include <binder/IServiceManager.h>
#include "JNIHelp.h"
using namespace android;
static jboolean hasMediaClient(JNIEnv *env, jobject thiz) {
	sp <IBinder> binder = defaultServiceManager()->getService(String16("media.player"));
	sp <IMediaPlayerService> service = interface_cast <IMediaPlayerService> (binder);
	if (service.get() == NULL) {
		jniThrowException(env, "java/lang/RuntimeException",
				"cannot get MediaPlayerService");
		return UNKNOWN_ERROR;
	}

	return service->hasMediaClient();
}

static jint getCurrentPostion(JNIEnv *env, jobject thiz){
	return 1;
}

static const char *classPathName = "com/rockchips/mediacenter/util/MediaUtils";

static JNINativeMethod methods[] = {
  {"hasMediaClient", "()Z", (void*)hasMediaClient },
  {"getCurrentPostion", "()I", (void*)getCurrentPostion }
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        ALOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env)
{
  if (!registerNativeMethods(env, classPathName,
                 methods, sizeof(methods) / sizeof(methods[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}


// ----------------------------------------------------------------------------

/*
 * This is called by the VM when the shared library is first loaded.
 */

typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;

    ALOGI("JNI_OnLoad");

    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        ALOGE("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

bail:
    return result;
}
