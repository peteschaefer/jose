
#include <signal.h>
#include "unixUtils.h"

JNIEXPORT jint JNICALL Java_de_jose_util_UnixUtils_kill
  (JNIEnv * env, jclass clazz, jint pid, jint sig)
{
   return kill(pid,sig);
}
