#include <pthread.h>
#include <jni.h>
#include <android/log.h>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <algorithm>

//#include <thread>
#include "../lib/etana/proof.h"
#include "../include/mklab.h"
extern "C" {
#include "../include/HTS_engine.h"
}


static const char* kTAG = "EKI_Synth";
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, kTAG, __VA_ARGS__))


CDisambiguator Disambiguator;
CLinguistic Linguistic;

typedef unsigned char uchar;

wchar_t *UTF8_to_WChar(const char *string) {
    long b = 0,
            c = 0;
    for (const char *a = string; *a; a++)
        if (((uchar) * a) < 128 || (*a & 192) == 192)
            c++;
    wchar_t *res = new wchar_t[c + 1];

    res[c] = 0;
    for (uchar *a = (uchar*) string; *a; a++) {
        if (!(*a & 128))
            res[b] = *a;
        else if ((*a & 192) == 128)
            continue;
        else if ((*a & 224) == 192)
            res[b] = ((*a & 31) << 6) | (a[1] & 63);
        else if ((*a & 240) == 224)
            res[b] = ((*a & 15) << 12) | ((a[1] & 63) << 6) | (a[2] & 63);
        else if ((*a & 248) == 240) {
            res[b] = '?';
        }
        b++;
    }
    return res;
}

void ReadUTF8Text(CFSWString &text, const char *fn) {

    std::ifstream fs;
    fs.open(fn, std::ios::binary);
    if (fs.fail()) {
        fprintf(stderr,"Ei leia sisendteksti!\n");
        exit(1);
    }
    fs.seekg(0, std::ios::end);
    size_t i = fs.tellg();    
    fs.seekg(0, std::ios::beg);
    char* buf = new char[i+1];
    fs.read(buf, i);
    fs.close();
    buf[i] = '\0';
    wchar_t* w_temp = UTF8_to_WChar(buf);
    text = w_temp;
    delete [] buf;
    delete [] w_temp;
}

void ConvertUTF8Text(CFSWString &text, const char *rawText) {
    wchar_t* w_temp = UTF8_to_WChar(rawText);
    text = w_temp;

    delete [] w_temp;
}

int PrintUsage() {
    fprintf(stderr,"\t-f 	[sisendtekst utf8-s] \n");
    fprintf(stderr,"\t-o 	[väljund-wav]  \n");
    fprintf(stderr,"\t-lex 	[analüüsi sõnastik]  \n");
    fprintf(stderr,"\t-lexd	[ühestaja sõnastik]  \n");
    fprintf(stderr,"\t-m 	[hääle nimi, vt kataloogi htsvoices/] \n");
    fprintf(stderr,"\t-r 	[kõnetempo, double, 0.01-2.76] \n");
    fprintf(stderr,"\t-ht 	[float]\n");
    fprintf(stderr,"\t-gvw1 	[float]\n");
    fprintf(stderr,"\t-gvw2 	[float]\n");
    fprintf(stderr,"\t-utt 	[prindi lausung]\n");
    fprintf(stderr,"\t-debug 	[prindi labeli struktuur]\n");
    fprintf(stderr,"\t-raw 	[väljund-raw]\n");
    fprintf(stderr,"\t-dur 	[foneemid koos kestustega, failinimi]\n");
    fprintf(stderr,"\n\tnäide: \n");
    fprintf(stderr,"\t\tbin/synthts_et -lex dct/et.dct -lexd dct/et3.dct \\ \n");
    fprintf(stderr,"\t\t-o out_tnu.wav -f in.txt -m htsvoices/eki_et_tnu.htsvoice \\\n");
    fprintf(stderr,"\t\t-r 1.1\n");
        
    exit(0);
}

char *convert_vec(const std::string & s) {
    char *pc = new char[s.size() + 1];
    strcpy(pc, s.c_str());
    return pc;
}

void fill_char_vector(std::vector<std::string>& v, std::vector<char*>& vc) {
    std::transform(v.begin(), v.end(), std::back_inserter(vc), convert_vec);
}

void clean_char_vector(std::vector<char*>& vc) {
    for (size_t x = 0; x < vc.size(); x++)
        delete [] vc[x];
}

std::string to_stdstring(CFSWString s) {
    std::string res = "";
    for (INTPTR i = 0; i < s.GetLength(); i++)
        res += s.GetAt(i);
    return res;
}

std::vector<std::string> to_vector(CFSArray<CFSWString> arr) {
    std::vector<std::string> v;
    for (INTPTR i = 0; i < arr.GetSize(); i++)
        v.push_back(to_stdstring(arr[i]));
    return v;
}

void cfileexists(const char * filename) {
    FILE *file;
    if (file = fopen(filename, "r")) {
        fclose(file);
        remove(filename);
    }
}

void samplerate(size_t &fr, size_t &fp, float &alpha, size_t br) {
    fr = br * 1000;
    fp = br / 2 * 10;

    if (fr <= 8000) alpha = 0.31; 
        else
    if (fr <= 10000) alpha = 0.35; 
        else
    if (fr <= 12000) alpha = 0.37; 
        else
    if (fr <= 16000) alpha = 0.42; 
        else
    if (fr <= 32000) alpha = 0.45; 
        else
    if (fr <= 44100) alpha = 0.53; 
        else
    if (fr <= 48000) alpha = 0.55; 
        else
            alpha = 0.55;
}

int main(int argc, char* argv[]) {
    size_t num_voices;
    char **fn_voices;
    char* in_fname;
    char* output_fname;
    FILE * outfp;
    char* dur_fname;
    FILE * durfp;    
    bool print_label = false;
    bool print_utt = false;
    bool write_raw = false;
    bool write_durlabel = false;

    CFSAString LexFileName, LexDFileName;
    HTS_Engine engine;
    double speed = 1.1;
    size_t fr = 48000;
    size_t fp = 240;
    float alpha = 0.55;
    float beta = 0.0;
    float ht = 1.0;
    float th = 0.5;
    float gvw1 = 1.0;
    float gvw2 = 1.2;

    FSCInit();
    fn_voices = (char **) malloc(argc * sizeof (char *));
    
    if (argc < 11) {
        fprintf(stderr, "Viga: liiga vähe parameetreid\n\n");
        PrintUsage();
    }    

    for (int i = 0; i < argc; i++) {
        if (CFSAString("-lex") == argv[i]) {
            if (i + 1 < argc) {
                LexFileName = argv[++i];
            } else {
                return PrintUsage();
            }
        }
        if (CFSAString("-lexd") == argv[i]) {
            if (i + 1 < argc) {
                LexDFileName = argv[++i];
            } else {
                return PrintUsage();
            }
        }
        if (CFSAString("-m") == argv[i]) {
            if (i + 1 < argc) {
                fn_voices[0] = argv[i + 1];
            } else {
                fprintf(stderr, "Viga: puudub *.htsvoice fail\n");
                PrintUsage();
                exit(0);
            }
        }
        if (CFSAString("-o") == argv[i]) {
            if (i + 1 < argc) {
                output_fname = argv[i + 1];
                cfileexists(output_fname);
            } else {
                fprintf(stderr, "Viga: puudb väljundfaili nimi\n");
                PrintUsage();
                exit(0);
            }
        }
        if (CFSAString("-f") == argv[i]) {
            if (i + 1 < argc) {
                in_fname = argv[i + 1];
            } else {
                fprintf(stderr, "Viga: puudb sisendfaili nimi\n");
                PrintUsage();
                exit(0);
            }
        }
        if (CFSAString("-s") == argv[i]) {
            if (i + 1 < argc) {
                samplerate(fr, fp, alpha, atoi(argv[i + 1]));
            }
        }
        if (CFSAString("-r") == argv[i]) {
            if (i + 1 < argc) {
                speed = atof(argv[i + 1]);
            }
        }
        if (CFSAString("-ht") == argv[i]) {
            if (i + 1 < argc) {
                ht = atof(argv[i + 1]);
            }
        }
        if (CFSAString("-gvw1") == argv[i]) {
            if (i + 1 < argc) {
                gvw1 = atof(argv[i + 1]);
            }
        }
        if (CFSAString("-gvw2") == argv[i]) {
            if (i + 1 < argc) {
                gvw2 = atof(argv[i + 1]);
            }
        }        
        if (CFSAString("-debug") == argv[i]) {
            print_label = true;
        }
        if (CFSAString("-utt") == argv[i]) {
            print_utt = true;
        }        
        if (CFSAString("-raw") == argv[i]) {
            write_raw = true;
        }
        if (CFSAString("-dur") == argv[i]) {
            if (i + 1 < argc) {
                dur_fname = argv[i + 1];
                cfileexists(dur_fname);
                write_durlabel = true;                
            } else {
                fprintf(stderr, "Viga: puudb kestustefaili nimi\n");
                PrintUsage();
                exit(0);
            }
        }

        
    }

    Linguistic.Open(LexFileName);
    Disambiguator.Open(LexDFileName);

    CFSWString text;
    ReadUTF8Text(text, in_fname);
    HTS_Engine_initialize(&engine);

    if (HTS_Engine_load(&engine, fn_voices, 1) != TRUE) {
        fprintf(stderr, "Viga: puudub *.htsvoice. %p\n", fn_voices[0]);
        free(fn_voices);
        HTS_Engine_clear(&engine);
        exit(1);
    }
    free(fn_voices);

    HTS_Engine_set_sampling_frequency(&engine, (size_t) fr);
    HTS_Engine_set_phoneme_alignment_flag(&engine, FALSE);
    HTS_Engine_set_fperiod(&engine, (size_t) fp);
    HTS_Engine_set_alpha(&engine, alpha);
    HTS_Engine_set_beta(&engine, beta);
    HTS_Engine_set_speed(&engine, speed);
    HTS_Engine_add_half_tone(&engine, ht);
    HTS_Engine_set_msd_threshold(&engine, 1, th);
    /*
    HTS_Engine_set_duration_interpolation_weight(&engine, 1, diw);
    HTS_Engine_set_parameter_interpolation_weight(&engine, 0, 0, piw1);
    HTS_Engine_set_parameter_interpolation_weight(&engine, 0, 1, piw2);
    HTS_Engine_set_gv_interpolation_weight(&engine, 0, 0, giw1);
    HTS_Engine_set_gv_interpolation_weight(&engine, 0, 1, giw2);
     */
    HTS_Engine_set_gv_weight(&engine, 0, gvw1);
    HTS_Engine_set_gv_weight(&engine, 1, gvw2);

    text = DealWithText(text);
    CFSArray<CFSWString> res = do_utterances(text);

    INTPTR data_size = 0;
    outfp = fopen(output_fname, "wb");
    if (write_durlabel) durfp = fopen(dur_fname, "w");
    if (!write_raw) HTS_Engine_write_header(&engine, outfp, 1);
    for (INTPTR i = 0; i < res.GetSize(); i++) {

        CFSArray<CFSWString> label = do_all(res[i], print_label, print_utt);

        std::vector<std::string> v;
        v = to_vector(label);

        std::vector<char*> vc;
        fill_char_vector(v, vc);

        size_t n_lines = vc.size();

        if (HTS_Engine_synthesize_from_strings(&engine, &vc[0], n_lines) != TRUE) {
            fprintf(stderr, "Viga: süntees ebaonnestus.\n");            
            HTS_Engine_clear(&engine);
            exit(1);
        }

        clean_char_vector(vc);
        data_size += HTS_Engine_engine_speech_size(&engine);
        if (write_durlabel) HTS_Engine_save_durlabel(&engine, durfp);
        HTS_Engine_save_generated_speech(&engine, outfp);

        HTS_Engine_refresh(&engine);

    } //synth loop
    
    if (!write_raw) HTS_Engine_write_header(&engine, outfp, data_size);
    if (write_durlabel) fclose(durfp);
    fclose(outfp);

    HTS_Engine_clear(&engine);
    Linguistic.Close();

    FSCTerminate();
    return 0;

}

HTS_Engine engineGlobal;

bool doInit(char * lex, char * lexd, char * htsVoice)
{
    char **fn_voices;
    CFSAString LexFileName, LexDFileName;
//    HTS_Engine engine;
    double speed = 1.1;
    size_t fr = 48000;
    size_t fp = 240;
    float alpha = 0.55;
    float beta = 0.0;
    float ht = 1.0;
    float th = 0.5;
    float gvw1 = 1.0;
    float gvw2 = 1.2;

    FSCInit();
    fn_voices = (char **) malloc(/*argc*/11 * sizeof (char *));

    LexFileName = lex;
    LexDFileName = lexd;
    fn_voices[0] = htsVoice;

    Linguistic.Open(LexFileName);
    Disambiguator.Open(LexDFileName);

    HTS_Engine_initialize(&engineGlobal);

    HTS_Boolean isEnginLoaded = HTS_Engine_load(&engineGlobal, fn_voices, 1);
    free(fn_voices);
    if (isEnginLoaded != TRUE) {
        HTS_Engine_clear(&engineGlobal);
        return false;
    }

    HTS_Engine_set_sampling_frequency(&engineGlobal, (size_t) fr);
    HTS_Engine_set_phoneme_alignment_flag(&engineGlobal, FALSE);
    HTS_Engine_set_fperiod(&engineGlobal, (size_t) fp);
    HTS_Engine_set_alpha(&engineGlobal, alpha);
    HTS_Engine_set_beta(&engineGlobal, beta);
    HTS_Engine_set_speed(&engineGlobal, speed);
    HTS_Engine_add_half_tone(&engineGlobal, ht);
    HTS_Engine_set_msd_threshold(&engineGlobal, 1, th);
    HTS_Engine_set_gv_weight(&engineGlobal, 0, gvw1);
    HTS_Engine_set_gv_weight(&engineGlobal, 1, gvw2);

    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_ee_eki_ekisynt_Util_initHTS(JNIEnv* env, jobject thisObj, jstring jInitFolder, jstring jLexStr, jstring jLexdStr, jstring jHtsVoice)
{

    char path[255];
    strcpy(path, env->GetStringUTFChars(jInitFolder, 0));
    strcat(path, "\0");

    char lex[255];
    strcpy(lex, path);
    strcat(lex, env->GetStringUTFChars(jLexStr, 0));
    strcat(lex, "\0");

    char lexd[255];
    strcpy(lexd, path);
    strcat(lexd, env->GetStringUTFChars(jLexdStr, 0));
    strcat(lexd, "\0");

    char hts_voice[255];
    strcpy(hts_voice, path);
    strcat(hts_voice, env->GetStringUTFChars(jHtsVoice, 0));
    strcat(hts_voice, "\0");

    bool isSuccess = doInit(lex, lexd, hts_voice);

    return isSuccess;
}

void terminate_HTS()
{
    HTS_Engine_clear(&engineGlobal);
    Linguistic.Close();
    Disambiguator.Close();

    FSCTerminate();
}

extern "C"
JNIEXPORT void JNICALL
Java_ee_eki_ekisynt_Util_shutDownHTS(JNIEnv* env, jobject thisObj)
{
    terminate_HTS();
}


typedef struct f_synth_context {
    JNIEnv  *   env;
    jobject     jCallback;
    jmethodID   audioAvailableMethodID;

    char    *   input_text;

    int         max_buf_size;
    int         buf_length;
    char    *   buffer;

    bool        is_synth_complete;
    bool        force_stop;

    pthread_mutex_t mutex;
    pthread_cond_t  condition_value;

    pthread_t   synth_thread;
    pthread_t   help_thread;
} SynthCtx;


SynthCtx sctx;

void stop_any_work() {
    sctx.force_stop = true;
}

extern "C"
JNIEXPORT void JNICALL
Java_ee_eki_ekisynt_Util_stopAnyWork(JNIEnv* env, jobject thisObj)
{
    stop_any_work();
}

void * f_gen_speech(void * context) {

    LOGI("Help_thread: started work");

    SynthCtx * ctx = (SynthCtx *) context;

    bool print_label = false;
    bool print_utt = false;

    CFSWString text;
    ConvertUTF8Text(text, ctx->input_text);
    text = DealWithText(text);
    CFSArray<CFSWString> res = do_utterances(text);

    int data_total_size = 0;
    int synth_data_size = 0;


    LOGI("Help_thread: utterances count - %d", (int)res.GetSize());

    for (INTPTR i = 0; i < res.GetSize(); i++) {

        HTS_Engine_refresh(&engineGlobal);

        if (sctx.force_stop) {
            LOGI("Help_thread: force stop! Buffer is free");
            break;
        }

        CFSArray<CFSWString> label = do_all(res[i], print_label, print_utt);

        LOGI("Help_thread: processing utterance - %d", i);

        std::vector<std::string> v;
        v = to_vector(label);

        std::vector<char*> vc;
        fill_char_vector(v, vc);

        size_t n_lines = vc.size();

        if (HTS_Engine_synthesize_from_strings(&engineGlobal, &vc[0], n_lines) != TRUE) {
            return false;
        }

        LOGI("Help_thread: synthesized from strings");

        clean_char_vector(vc);

        synth_data_size = HTS_Engine_engine_speech_size(&engineGlobal);
        char * synth_buffer = (char *) malloc(synth_data_size);

        LOGI("Help_thread: allocated memory for the data");

        HTS_Engine_put_generated_speech(&engineGlobal, synth_buffer, 0);

        LOGI("Help_thread: data copied into buffer");

        if (sctx.force_stop) {
            free(synth_buffer);
            LOGI("Help_thread: force stop! Buffer is free");
            break;
        }

        /* LOCK MUTEX TO SEND SIGNAL*/
        pthread_mutex_lock(&(ctx->mutex));

        LOGI("Help_thread: lock mutex");

        ctx->buffer = synth_buffer;
        ctx->buf_length = synth_data_size;

        if (i >= res.GetSize() - 1) {
            ctx->is_synth_complete = true;
            LOGI("Help_thread: synth_complete flag is true");
        }

        LOGI("Help_thread: sending signal");
        pthread_cond_signal(&(ctx->condition_value));

        /* UNLOCK MUTEX*/
        pthread_mutex_unlock(&(ctx->mutex));

        LOGI("Help_thread: unlock mutex");

        data_total_size += synth_data_size;

//        HTS_Engine_refresh(&engineGlobal);

    } //synth loop

    ctx->is_synth_complete = true;
    LOGI("Help_thread: synth_complete flag is true. Help thread is DONE!");

    if (sctx.force_stop) {
        // send signal to unlock synth_thread if last waiting for a signal
        LOGI("Help_thread: sending signal...");
        pthread_cond_signal(&(ctx->condition_value));
    }

    return NULL;
}

bool f_send_buf(SynthCtx ctx) {

    jbyteArray jbuf = ctx.env->NewByteArray(ctx.max_buf_size);

    ctx.synth_thread = pthread_self();

    pthread_attr_t attr_t;
    pthread_attr_init(&attr_t);

    pthread_mutex_init(&(ctx.mutex), NULL);
    pthread_cond_init(&(ctx.condition_value), NULL);

    struct timeval now;
    struct timespec timeout;
    int attempt = 0;

    LOGI("Mutex and cond_value initialized");

    ctx.is_synth_complete = false;
    pthread_create(&(ctx.help_thread), &attr_t, f_gen_speech, &ctx);

    LOGI("Help thread created!");

    /* LOCK  (to make the pointer on the buffer accessible )*/
    pthread_mutex_lock(&(ctx.mutex));
    LOGI("Synth_thread: lock mutex");

    while (!(ctx.is_synth_complete && ctx.buffer == NULL)) {

        if (sctx.force_stop) {
            LOGI("Synth_thread: force stop...");

            if (ctx.buffer != NULL) {
                free(ctx.buffer);
                ctx.buffer = NULL;
                LOGI("Synth_thread: allocated memory is free");
            }
            break;
        }

        /* WAIT FOR RESULTS*/
        attempt = 0;
        while (ctx.buffer == NULL && !sctx.force_stop) {
            // to prevent an endless wait
            if (attempt < 2) {
                attempt++;
            } else {
                sctx.force_stop = true;
                break;
            }

            gettimeofday(&now, 0);
            timeout.tv_sec = now.tv_sec + 15;      // 15 sec
            timeout.tv_nsec = now.tv_usec * 1000;

            LOGI("Synth_thread: start waiting...");
            pthread_cond_timedwait(&(ctx.condition_value), &(ctx.mutex), &timeout);
        }

        if (sctx.force_stop) {
            LOGI("Synth_thread: force stop....");

            if (ctx.buffer != NULL) {
                free(ctx.buffer);
                ctx.buffer = NULL;
                LOGI("Synth_thread: allocated memory is free");
            }

            break;
        }

        LOGI("Synth_thread: continue... sending data to play");

        /* SEND BUFFER  (should be in the synthesis thread only!!!) */
        int copying_bytes_count = 0;
        int buffered_data_size = 0;
        int offset = 0;
        int bytes_left = ctx.buf_length;

        while (bytes_left > 0) {
            if (sctx.force_stop) {
                LOGI("Synth_thread: force stop.....");
                break;
            }

            LOGI("Synth_thread: bytes left: %d", bytes_left);

            copying_bytes_count = (ctx.max_buf_size - buffered_data_size < bytes_left) ?
                                  ctx.max_buf_size - buffered_data_size : bytes_left;
            buffered_data_size += copying_bytes_count;

            ctx.env->SetByteArrayRegion(jbuf, 0, buffered_data_size, (jbyte*)ctx.buffer + offset);
            ctx.env->CallIntMethod(ctx.jCallback, ctx.audioAvailableMethodID,
                                    jbuf, 0, buffered_data_size);

            offset += copying_bytes_count;
            buffered_data_size = 0;

            bytes_left -= copying_bytes_count;
        }

        LOGI("Synth_thread:  bytes left: %d", bytes_left);

        free(ctx.buffer);
        ctx.buffer = NULL;
        LOGI("Synth_thread: allocated memory is free");

    }

    /* UNLOCK */
    pthread_mutex_unlock(&(ctx.mutex));
    LOGI("Synth_thread: unlock mutex");

    if (sctx.is_synth_complete) {
        LOGI("Synth_thread: The stop_generatieng flag is true!");
    }


    LOGI("Synth_thread: wait until help_thread terminating...");
    pthread_join(ctx.help_thread, NULL);

    LOGI("Synth_thread: trying to release mutex and cond_value...");
    pthread_mutex_destroy(&ctx.mutex);
    pthread_cond_destroy(&ctx.condition_value);
    LOGI("Synth_thread: Mutex and cond_value are free!");


    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_ee_eki_ekisynt_Util_synthTextHTS(JNIEnv* env, jobject thisObj, jstring jText, jint textLength, jint jmaxBufSize, jdouble rate, jfloat ht, jobject jCallback)
{
    bool result = true;
    char text[textLength];
    strcpy(text, env->GetStringUTFChars(jText, 0));

    strcat(text, "\0");

    double speed = rate;
    HTS_Engine_set_speed(&engineGlobal, speed);
    float aht = ht;
    HTS_Engine_add_half_tone(&engineGlobal, aht);

//    SynthCtx sctx;

    memset(&sctx, 0, sizeof(sctx));
    jclass myCallbackClass = env->GetObjectClass(jCallback);

    sctx.env = env;
    sctx.jCallback = jCallback;
    sctx.audioAvailableMethodID = env->GetMethodID(myCallbackClass, "audioAvailable", "([BII)I");

    sctx.max_buf_size = jmaxBufSize;
    sctx.buf_length = 0;
    sctx.buffer = NULL;

    sctx.force_stop = false;

    sctx.input_text = text;

    LOGI("Context initialized!");

    f_send_buf(sctx);

    LOGI("DONE! (f_send_buf)");

    LOGI("End synth!!!");

    return (jboolean)result;
}