/**************************************************************************/
/*
/* junixsocket.c -- Part of the org.lirc.socket package
/* Copyright (C) 2001-2002 Bjorn Bringert (bjorn@mumblebee.com)
/*
/* This program is free software; you can redistribute it and/or
/* modify it under the terms of the GNU General Public License
/* as published by the Free Software Foundation; either version 2
/* of the License, or (at your option) any later version.
/*
/* This program is distributed in the hope that it will be useful,
/* but WITHOUT ANY WARRANTY; without even the implied warranty of
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/* GNU General Public License for more details.
/*
/* You should have received a copy of the GNU General Public License
/* along with this program; if not, write to the Free Software
/* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
/*
/**************************************************************************/

#include "junixsocket.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>

static jclass io_exc_cls = NULL;
static jfieldID fd_fid = NULL;


/*
 * Throws an IOException
 */
static void throwIOExc(JNIEnv *env, char *msg){
	if (io_exc_cls == NULL) {
		jclass io_exc_cls0 = (*env)->FindClass(env, "java/io/IOException");
		if (io_exc_cls0 == NULL) {
			return;
		}
		io_exc_cls = (*env)->NewGlobalRef(env, io_exc_cls0);
		if (io_exc_cls == NULL) {
			return;
		}
	}
	(*env)->ThrowNew(env, io_exc_cls, msg);
}

/*
 * Unlinks a file if it is a socket.
 * @return 1 if the file does not exist or if it was a socket and
 * could be unlinked, 0 otherwise
 */
static int unlink_if_socket(JNIEnv *env, char *path) {
	struct stat stbuf;

	if (stat(path, &stbuf) == 0) {
		if (S_ISSOCK(stbuf.st_mode)) {
			if (unlink(path) == 0) {
#ifdef DEBUG
				printf("junixsocket: Unlinked %s\n", path);
#endif
				return 1;
			} else {
				throwIOExc(env, strerror(errno));
				return 0;
			}
		} else {
			throwIOExc(env, "File is not a socket");
			return 0;
		}
	} else if (errno == ENOENT) {
		return 1; // File doesn't exist, so we're okay
	} else {
		throwIOExc(env, strerror(errno));
		return 0;
	}
}

//
// UnixSocketImpl
//


/*
 * Gets the value of the fd field in UnixSocketImpl
 * Throws IOException if fd == -1
 */
static int getFd(JNIEnv *env, jobject obj) {
	int fd;
	if (fd_fid == NULL) {
		jclass cls = (*env)->GetObjectClass(env, obj);
		fd_fid = (*env)->GetFieldID(env, cls, "fd", "I");
		if (fd_fid == NULL) {
			return -1;
		}
	}
	fd = (*env)->GetIntField(env, obj, fd_fid);
	if (fd < 0) {
		throwIOExc(env, "Socket not created");
		return -1;
	}
	return fd;
}

JNIEXPORT jint JNICALL Java_org_lirc_socket_UnixSocketImpl_createSocket
		(JNIEnv *env, jclass cls) {
	int fd = socket(AF_UNIX, SOCK_STREAM, 0);
	if (fd < 0) { // handle socket creation errors
		throwIOExc(env, strerror(errno));
		return -1;
	}

#ifdef DEBUG
	printf("junixsocket: Created socket %i\n", fd);
#endif

	return fd;
}

JNIEXPORT void JNICALL Java_org_lirc_socket_UnixSocketImpl_bind
		(JNIEnv *env, jobject obj, jstring jmyPath) {
	int fd;
	struct sockaddr_un addr;
	const char *socket_name;

	// get fd
	fd = getFd(env, obj);

	// set up addr
	addr.sun_family = AF_UNIX;
	socket_name = (*env)->GetStringUTFChars(env, jmyPath, NULL);
	strcpy(addr.sun_path, socket_name);
	(*env)->ReleaseStringUTFChars(env, jmyPath, socket_name);

	if (unlink_if_socket(env, addr.sun_path) == 0) {
		return;
	}

#ifdef DEBUG
	printf("junixsocket: binding %i to %s\n", fd, addr.sun_path);
#endif

	// bind
	if (bind(fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
		close(fd);
		throwIOExc(env, strerror(errno));
		return;
	}
}

JNIEXPORT void JNICALL Java_org_lirc_socket_UnixSocketImpl_listen
		(JNIEnv *env, jobject obj, jint backlog) {
	// get fd
	int fd = getFd(env, obj);

#ifdef DEBUG
	printf("junixsocket: listen(%i, %i)\n", fd, backlog);
#endif

	// listen
	if (listen(fd, backlog) < 0) {
		close(fd);
		throwIOExc(env, strerror(errno));
		return;
	}
}

JNIEXPORT jint JNICALL Java_org_lirc_socket_UnixSocketImpl_nativeAccept
		(JNIEnv *env, jobject obj) {
	struct sockaddr_un client_addr;
	size_t client_addr_len;
	int fd;
	int client_fd;

	// get fd
	fd = getFd(env, obj);

	// accept
	client_addr_len = sizeof(client_addr);
	bzero((char *)&client_addr, client_addr_len);
	client_fd = accept(fd, (struct sockaddr *)&client_addr, &client_addr_len);
	if (client_fd < 0) {
		throwIOExc(env, strerror(errno));
		return -1;
	}

#ifdef DEBUG
	printf("junixsocket: Accepted %i from \"%s\" (%i)\n", fd, client_addr.sun_path, client_addr_len);
#endif

	return client_fd;
}

JNIEXPORT void JNICALL Java_org_lirc_socket_UnixSocketImpl_connect
		(JNIEnv *env, jobject obj, jstring jpath) {
	// get fd
	int fd = getFd(env, obj);

	// set up addr
	struct sockaddr_un addr;
	const char *socket_name;
	addr.sun_family = AF_UNIX;
	socket_name = (*env)->GetStringUTFChars(env, jpath, NULL);
	strcpy(addr.sun_path, socket_name);
	(*env)->ReleaseStringUTFChars(env, jpath, socket_name);

#ifdef DEBUG
	printf("junixsocket: Connecting %i to %s\n", fd, addr.sun_path);
#endif

	// connect
	if (connect(fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
		close(fd);
		throwIOExc(env, strerror(errno));
		return;
	}
}

JNIEXPORT void JNICALL Java_org_lirc_socket_UnixSocketImpl_close
		(JNIEnv *env, jobject obj) {
	int fd = getFd(env, obj);

#ifdef DEBUG
	printf("junixsocket: Closing %i\n", fd);
#endif

	if (close(fd) < 0) {
		throwIOExc(env, strerror(errno));
		return;
	}
}

JNIEXPORT void JNICALL Java_org_lirc_socket_UnixSocketImpl_shutdownInput
		(JNIEnv *env, jobject obj) {
	int fd;
	fd = getFd(env, obj);

#ifdef DEBUG
	printf("junixsocket: Shutting down input for %i\n", fd);
#endif

	if (shutdown(fd, 0) < 0) {
		throwIOExc(env, strerror(errno));
		return;
	}
}

JNIEXPORT void JNICALL Java_org_lirc_socket_UnixSocketImpl_shutdownOutput
		(JNIEnv *env, jobject obj) {
	int fd;
	fd = getFd(env, obj);

#ifdef DEBUG
	printf("junixsocket: Shutting down output for %i\n", fd);
#endif

	if (shutdown(fd, 1) < 0) {
		throwIOExc(env, strerror(errno));
		return;
	}
}




//
// UnixSocketInputStream
//

JNIEXPORT jint JNICALL Java_org_lirc_socket_UnixSocketInputStream_nativeRead__I
		(JNIEnv * env, jclass cls, jint fd) {
	int i;
	char buffer[] = { 0 };

	i = recv(fd, buffer, 1, 0);

	if (i == 0) {
		return -1;
	} else if (i < 0) {
		throwIOExc(env, strerror(errno));
		return -1;
	}

#ifdef DEBUG
	printf("junixsocket: Read '%c' from %i\n", buffer[0], fd);
#endif

	return buffer[0];
}

JNIEXPORT jint JNICALL Java_org_lirc_socket_UnixSocketInputStream_nativeRead__I_3BII
		(JNIEnv *env, jclass cls, jint fd, jbyteArray jbuf, jint off, jint len) {
	int i;
	jbyte *buf;

	buf = (*env)->GetByteArrayElements(env, jbuf, NULL);
	if (buf == NULL) {
		return -1; // something went wrong getting the jni array
	}
	i = recv(fd, buf+off, len, 0);
	(*env)->ReleaseByteArrayElements(env, jbuf, buf, 0);

	if (i == 0) {
		return -1;
	} else if (i < 0) {
		throwIOExc(env, strerror(errno));
		return -1;
	}

#ifdef DEBUG
	printf("junixsocket: Read %i bytes from %i\n", i, fd);
#endif

	return i;
}


//
// UnixSocketOutputStream
//

JNIEXPORT void JNICALL Java_org_lirc_socket_UnixSocketOutputStream_nativeWrite__II
		(JNIEnv *env, jclass cls, jint fd, jint b) {
	char buffer[] = { b };
	if (send(fd, buffer, 1, 0) < 0) {
		throwIOExc(env, strerror(errno));
		return;
	}

#ifdef DEBUG
	printf("junixsocket: Wrote '%c' to %i\n", b, fd);
#endif

}

JNIEXPORT void JNICALL Java_org_lirc_socket_UnixSocketOutputStream_nativeWrite__I_3BII
		(JNIEnv *env, jclass cls, jint fd, jbyteArray jbuf, jint off, jint len) {

	jbyte *buf;
	int r;

	buf = (*env)->GetByteArrayElements(env, jbuf, NULL);
	if (buf == NULL) {
		return; // something went wrong getting the jni array
	}
	r = send(fd, buf+off, len, 0);
	(*env)->ReleaseByteArrayElements(env, jbuf, buf, 0);

	if (r < 0) {
		throwIOExc(env, strerror(errno));
		return;
	}

#ifdef DEBUG
	printf("junixsocket: Wrote %i bytes to %i\n", len, fd);
#endif

}
