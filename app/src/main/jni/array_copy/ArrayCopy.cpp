//
// Created by Tony Wang on 8/7/15.
//

#include "ArrayCopy.h"
#include <stdint.h>
#include <cassert>

#ifdef __cplusplus
extern "C" {
#endif

void stereo_to_mono_c(void *left,
                      void *right,
                      const void *src,
                      size_t length);

JNIEXPORT jboolean JNICALL Java_org_jt_performance_ArrayCopy_deliverArray
(JNIEnv *env, jobject obj, jbyteArray jsrc, jbyteArray jleft, jbyteArray jright, jint length)
{
    jboolean isCopy = JNI_FALSE;
    jbyte *src = env->GetByteArrayElements(jsrc, &isCopy);
    jbyte *left = env->GetByteArrayElements(jleft, &isCopy);
    jbyte *right = env->GetByteArrayElements(jright, &isCopy);

//    for(int i = 0; i < length / 4; i++)
//    {
//        int index = i << 2;
//        int half = i << 1;
//        left[half] = src[index];
//        left[half + 1] = src[index + 1];
//        right[half] = src[index + 2];
//        right[half + 1] = src[index + 3];
//    }

    stereo_to_mono_c(left, right, src, length);

    env->ReleaseByteArrayElements(jsrc, src, JNI_ABORT);
    env->ReleaseByteArrayElements(jleft, left, 0);
    env->ReleaseByteArrayElements(jright, right, 0);

    env->DeleteLocalRef(obj);
    env->DeleteLocalRef(jsrc);
    env->DeleteLocalRef(jleft);
    env->DeleteLocalRef(jright);
    return JNI_TRUE;
}

void stereo_to_mono_c(void *left,
                      void *right,
                      const void *src,
                      size_t length)
{
    assert(length % 2 == 0);
    assert(length > 16);
    // we assume length is greater than 2 times of sizeof(uint64_t) and length is even number

    uint8_t *left_ptr = static_cast<uint8_t *>(left);
    uint8_t *right_ptr = static_cast<uint8_t *>(right);
    const uint8_t *src_ptr = static_cast<const uint8_t *>(src);

    const size_t pre_bytes = (long)src_ptr % sizeof(uint64_t);
    const size_t word_size = (length - pre_bytes) / sizeof(uint64_t);
    const size_t suf_bytes = length - pre_bytes - word_size * sizeof(uint64_t);

    const uint8_t *pre_ptr = src_ptr;
    const uint64_t *word_ptr = (uint64_t *)(src_ptr + pre_bytes);
    const uint8_t *suf_ptr = src_ptr + pre_bytes + word_size * sizeof(uint64_t);

    for (size_t i = 0; i < pre_bytes / 2; i++) {
        *left_ptr++ = *pre_ptr++;
        *right_ptr++ = *pre_ptr++;
    }

    if (pre_bytes % 2 == 0) {
        for (size_t j = 0; j < word_size; ++j) {
            *left_ptr++ = *word_ptr & 0xff;
            *right_ptr++ = (*word_ptr >> 8) & 0xff;
            *left_ptr++ = (*word_ptr >> 16) & 0xff;
            *right_ptr++ = (*word_ptr >> 24) & 0xff;
            *left_ptr++ = (*word_ptr >> 32) & 0xff;
            *right_ptr++ = (*word_ptr >> 40) & 0xff;
            *left_ptr++ = (*word_ptr >> 48) & 0xff;
            *right_ptr++ = (*word_ptr++ >> 56) & 0xff;
        }

        for (size_t j = 0; j < suf_bytes / 2; j++) {
            *left_ptr++ = *suf_ptr++;
            *right_ptr++ = *suf_ptr++;
        }
    } else {
        *left_ptr++ = *pre_ptr;

        for (size_t j = 0; j < word_size; ++j) {
            *right_ptr++ = *word_ptr & 0xff;
            *left_ptr++ = (*word_ptr >> 8) & 0xff;
            *right_ptr++ = (*word_ptr >> 16) & 0xff;
            *left_ptr++ = (*word_ptr >> 24) & 0xff;
            *right_ptr++ = (*word_ptr >> 32) & 0xff;
            *left_ptr++ = (*word_ptr >> 40) & 0xff;
            *right_ptr++ = (*word_ptr >> 48) & 0xff;
            *left_ptr++ = (*word_ptr++ >> 56) & 0xff;
        }

        for (uint16_t j = 0; j < suf_bytes / 2; j++) {
            *right_ptr++ = *suf_ptr++;
            *left_ptr++ = *suf_ptr++;
        }

        *right_ptr++ = *suf_ptr++;
    }
}


#ifdef __cplusplus
}
#endif