# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.


cmake_minimum_required(VERSION 3.4.1)


set(ROOT_SRC_DIR src/main/synthts_et)
set(LIB_SRC_DIR src/main/synthts_et/lib)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             synthts_et

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${ROOT_SRC_DIR}/bin/synthts_et.cpp

#             ${ROOT_SRC_DIR}/dct/et.dct
#             ${ROOT_SRC_DIR}/dct/et3.dct
#
#             ${ROOT_SRC_DIR}/htsvoices/eki_et_eva.htsvoice
#             ${ROOT_SRC_DIR}/htsvoices/eki_et_evas.htsvoice
#             ${ROOT_SRC_DIR}/htsvoices/eki_et_evasl.htsvoice
#             ${ROOT_SRC_DIR}/htsvoices/eki_et_lis.htsvoice
#             ${ROOT_SRC_DIR}/htsvoices/eki_et_rna.htsvoice
#             ${ROOT_SRC_DIR}/htsvoices/eki_et_tnu.htsvoice
#
             ${LIB_SRC_DIR}/engine/HTS_audio.c
             ${LIB_SRC_DIR}/engine/HTS_engine.c
             ${LIB_SRC_DIR}/engine/HTS_engine_patches.c
             ${LIB_SRC_DIR}/engine/HTS_gstream.c
             ${LIB_SRC_DIR}/engine/HTS_label.c
             ${LIB_SRC_DIR}/engine/HTS_misc.c
             ${LIB_SRC_DIR}/engine/HTS_model.c
             ${LIB_SRC_DIR}/engine/HTS_pstream.c
             ${LIB_SRC_DIR}/engine/HTS_sstream.c
             ${LIB_SRC_DIR}/engine/HTS_vocoder.c

             ${LIB_SRC_DIR}/etana/adhoc.cpp
             ${LIB_SRC_DIR}/etana/arva_av.cpp
             ${LIB_SRC_DIR}/etana/arvahy1.cpp
             ${LIB_SRC_DIR}/etana/arvai.cpp
             ${LIB_SRC_DIR}/etana/arvalyh1.cpp
             ${LIB_SRC_DIR}/etana/arvalyh2.cpp
             ${LIB_SRC_DIR}/etana/arvamitte.cpp
             ${LIB_SRC_DIR}/etana/arvans1.cpp
             ${LIB_SRC_DIR}/etana/arvans2.cpp
             ${LIB_SRC_DIR}/etana/arvapn1.cpp
             ${LIB_SRC_DIR}/etana/arvapn2.cpp
             ${LIB_SRC_DIR}/etana/arvash1.cpp
             ${LIB_SRC_DIR}/etana/arvasuf1.cpp
             ${LIB_SRC_DIR}/etana/arvavi1.cpp
             ${LIB_SRC_DIR}/etana/arvax.cpp
             ${LIB_SRC_DIR}/etana/bins.cpp
             ${LIB_SRC_DIR}/etana/bsearch.cpp
             ${LIB_SRC_DIR}/etana/chkgeon.cpp
             ${LIB_SRC_DIR}/etana/chkhy1.cpp
             ${LIB_SRC_DIR}/etana/chkhy2.cpp
             ${LIB_SRC_DIR}/etana/chklyh0.cpp
             ${LIB_SRC_DIR}/etana/chklyh1.cpp
             ${LIB_SRC_DIR}/etana/chklyh2.cpp
             ${LIB_SRC_DIR}/etana/chklyh3.cpp
             ${LIB_SRC_DIR}/etana/chklyh4.cpp
             ${LIB_SRC_DIR}/etana/chkmin.cpp
             ${LIB_SRC_DIR}/etana/chkmitte.cpp
             ${LIB_SRC_DIR}/etana/chknr2.cpp
             ${LIB_SRC_DIR}/etana/chkoleta.cpp
             ${LIB_SRC_DIR}/etana/chkvaljend.cpp
             ${LIB_SRC_DIR}/etana/chkx.cpp
             ${LIB_SRC_DIR}/etana/close_d.cpp
             ${LIB_SRC_DIR}/etana/cmorf.cpp
             ${LIB_SRC_DIR}/etana/cxxcash.cpp
             ${LIB_SRC_DIR}/etana/cxxraam.cpp
             ${LIB_SRC_DIR}/etana/cyybs.cpp
             ${LIB_SRC_DIR}/etana/disambiguator.cpp
             ${LIB_SRC_DIR}/etana/echopath.cpp
             ${LIB_SRC_DIR}/etana/et3myh.cpp
             ${LIB_SRC_DIR}/etana/etmrfa.cpp
             ${LIB_SRC_DIR}/etana/etmrfverstr.cpp
             ${LIB_SRC_DIR}/etana/find_fd.cpp
             ${LIB_SRC_DIR}/etana/hjk_cxx.cpp
             ${LIB_SRC_DIR}/etana/kchk1.cpp
             ${LIB_SRC_DIR}/etana/kchk2.cpp
             ${LIB_SRC_DIR}/etana/kchk30.cpp
             ${LIB_SRC_DIR}/etana/kchk33.cpp
             ${LIB_SRC_DIR}/etana/kchk4.cpp
             ${LIB_SRC_DIR}/etana/kchk5.cpp
             ${LIB_SRC_DIR}/etana/kchk6.cpp
             ${LIB_SRC_DIR}/etana/kjuhtum.cpp
             ${LIB_SRC_DIR}/etana/komp.cpp
             ${LIB_SRC_DIR}/etana/kr6nksud1.cpp
             ${LIB_SRC_DIR}/etana/linguistic.cpp
             ${LIB_SRC_DIR}/etana/loefailist.cpp
             ${LIB_SRC_DIR}/etana/mittesona.cpp
             ${LIB_SRC_DIR}/etana/mrf-gen.cpp
             ${LIB_SRC_DIR}/etana/mrf2yh2mrf.cpp
             ${LIB_SRC_DIR}/etana/open_d1.cpp
             ${LIB_SRC_DIR}/etana/open_d2.cpp
             ${LIB_SRC_DIR}/etana/open_dm.cpp
             ${LIB_SRC_DIR}/etana/ptword.cpp
             ${LIB_SRC_DIR}/etana/readeel.cpp
             ${LIB_SRC_DIR}/etana/readends.cpp
             ${LIB_SRC_DIR}/etana/readfgrs.cpp
             ${LIB_SRC_DIR}/etana/readfms.cpp
             ${LIB_SRC_DIR}/etana/readgrs.cpp
             ${LIB_SRC_DIR}/etana/readloe.cpp
             ${LIB_SRC_DIR}/etana/readprf.cpp
             ${LIB_SRC_DIR}/etana/readsuf.cpp
             ${LIB_SRC_DIR}/etana/rfi.cpp
             ${LIB_SRC_DIR}/etana/rooma.cpp
             ${LIB_SRC_DIR}/etana/silp.cpp
             ${LIB_SRC_DIR}/etana/sloendid.cpp
             ${LIB_SRC_DIR}/etana/sobivus.cpp
             ${LIB_SRC_DIR}/etana/strcnv.cpp
             ${LIB_SRC_DIR}/etana/strtosi.cpp
             ${LIB_SRC_DIR}/etana/suggestor.cpp
             ${LIB_SRC_DIR}/etana/tloendid.cpp
             ${LIB_SRC_DIR}/etana/tmk2t.cpp
             ${LIB_SRC_DIR}/etana/tmk2tx.cpp
             ${LIB_SRC_DIR}/etana/ty_lp.cpp
             ${LIB_SRC_DIR}/etana/valjatr.cpp

             ${LIB_SRC_DIR}/fsc/fscinit.cpp
             ${LIB_SRC_DIR}/fsc/fsdata.cpp
             ${LIB_SRC_DIR}/fsc/fsdll.cpp
             ${LIB_SRC_DIR}/fsc/fsexception.cpp
             ${LIB_SRC_DIR}/fsc/fsfile.cpp
             ${LIB_SRC_DIR}/fsc/fsfixalloc.cpp
             ${LIB_SRC_DIR}/fsc/fshugeinteger.cpp
             ${LIB_SRC_DIR}/fsc/fslist.cpp
             ${LIB_SRC_DIR}/fsc/fsmemory.cpp
             ${LIB_SRC_DIR}/fsc/fsreg.cpp
             ${LIB_SRC_DIR}/fsc/fsstring.cpp
             ${LIB_SRC_DIR}/fsc/fsthread.cpp
             ${LIB_SRC_DIR}/fsc/fstime.cpp
             ${LIB_SRC_DIR}/fsc/fstrace.cpp
             ${LIB_SRC_DIR}/fsc/fswav.cpp

             ${LIB_SRC_DIR}/label/label.cpp
             ${LIB_SRC_DIR}/label/mklab.cpp
             ${LIB_SRC_DIR}/label/number.cpp
             ${LIB_SRC_DIR}/label/syls.cpp
             ${LIB_SRC_DIR}/label/util.cpp
             ${LIB_SRC_DIR}/label/word.cpp
             )

include_directories(${ROOT_SRC_DIR}/include/
#                    ${ROOT_SRC_DIR}/dct/
#                    ${ROOT_SRC_DIR}/htsvoices/
                    ${LIB_SRC_DIR}/engine/
                    ${LIB_SRC_DIR}/etana/
                    ${LIB_SRC_DIR}/fsc/
                    ${LIB_SRC_DIR}/label/
                    )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       synthts_et

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )