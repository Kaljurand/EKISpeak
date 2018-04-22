#include "../etana/proof.h"
#include "util.h"

bool print_syls = false;

const INTPTR pattern_word_end_size = 23;

CFSWString pattern_word_end[pattern_word_end_size] = {
    L"VV", L"VVQ", L"VVQQ", L"VVC", L"VVss", L"VVsQ", L"VVQs", L"VVLQ", L"VVLQQ", L"VVCC",
    L"VVCCC", L"V", L"VC", L"VLQ", L"VLQQ", L"VLh", L"VCC", L"VLss", L"VLsQ", L"VLhv",
    L"VLQC", L"VCCC", L"VCCCC"
};

const INTPTR shift_word_end[pattern_word_end_size] = {
    2, // VV#
    2, // VVQ#      Eek 3 l<aat -> laat:
    3, // VVQQ      ++
    2, // VVC#
    3, // VVss#     p<oiss
    2, // VVsQ#     l<aast
    3, // VVQs#     l<oots
    2, // VVLQ#     Eek 4 h<uult -> huult:
    4, // VVLQQ#    ++
    2, // VVCC#     k<eeld

    2, // VVCCC#    p<aavst
    1, // V#        niigi kaduvad j:a m:e jms?
    2, // VC#       k<as
    2, // VLQ#      Eek 3 k<urt -> kurt:
    3, // VLQQ#     ++
    3, // VLh#
    2, // VCC#
    3, // VLss#
    3, // VLsQ#
    3, // VLhv#

    3, // VLQC#
    2, // VCCC#
    2  // VCCCC#    ++
};

const INTPTR pattern_syll_end_size = 34;

CFSWString pattern_syll_end[pattern_syll_end_size] = {
    L"VV-Q", L"VV-", L"VVs-s", L"VVs-Q", L"VVL-Q", L"VVQ-L", L"VVQ-j", L"VVQ-", L"VVQQ-", L"VVC-",
    L"VVQs-", L"VVsQ-", L"VVLQ-", L"VVLQ-Q", L"VVLQQ-", L"VVCC-", L"VVCCC-", L"VL-Q", L"VL-h", L"VC-",
    L"VLQ-", L"VLQ-Q", L"VLQ-s", L"VLs-", L"VLh-", L"VCC-", L"VLsQ-", L"VLQC-", L"VCCC-", L"VLQCC-",
    L"VCCCC-", L"VC-C", L"V-", L"lopp"
};

const INTPTR shift_syll_end[pattern_syll_end_size] = {
    2, // VV-Q      Eek 4 l<aat-ta -> laat-t:a
    2, // VV-
    3, // VVs-s
    3, // VVs-Q
    5, // VVL-Q
    2, // VVQ-L     ++
    2, // VVQ-j     ++
    3, // VVQ-
    3, // VVQQ-     ++
    2, // VVC-

    3, // VVQs-
    3, // VVsQ-
    2, // VVLQ-     Eek 4 k<aart-lane -> kaart:-lane
    4, // VVLQ-Q    ++
    4, // VVLQQ-    ++
    2, // VVCC-
    2, // VVCCC-
    2, // VL-Q      Eek 4 k<ar-ta -> kar-t:a
    4, // VL-h
    2, // VC-

    2, // VLQ-      Eek 3 p<ilt-l<ik -> pilt:-lik:
    3, // VLQ-Q     ++
    3, // VLQ-s     ++
    3, // VLs-
    3, // VLh-
    2, // VCC-
    3, // VLsQ-
    3, // VLQC-
    2, // VCCC-
    3, // VLQCC-

    2, // VCCCC-
    2, // VC-C      ++
    1, // V-        ++ Nikol<ajeva (FS sõnastikus, aga kas ka keeleline?)
    2  // lopp
};

CFSWString simplify_pattern(CFSWString s) {
    CFSWString res;
    for (INTPTR i = 0; i < (s.GetLength()); i++) {
        CFSWString c = s.GetAt(i);
        if (c.FindOneOf(L"jhvsLQ") > -1)
            res += L"C";
        else
            res += c;
    }
    return res;
}

INTPTR pattern_lookup_word_end(CFSWString s) {
    INTPTR res = -1;
    // NB See ei võrdle viimast kirjet VCCCC
    for (INTPTR i = 0; i < (pattern_word_end_size - 1); i++) {
        if (s == pattern_word_end[i]) {
            res = shift_word_end[i];
            return res;
       }
    }
    s = simplify_pattern(s);
    for (INTPTR i = 0; i < (pattern_word_end_size - 1); i++) {
        if (s == pattern_word_end[i]) {
            res = shift_word_end[i];
            return res;
       }
    }
    return res;
}

INTPTR pattern_lookup_syll_end(CFSWString s) {
    INTPTR res = -1;
    for (INTPTR i = 0; i < (pattern_syll_end_size - 1); i++) {
        if (s == pattern_syll_end[i]) {
            res = shift_syll_end[i];
            return res;
       }
    }
    s = simplify_pattern(s);
    for (INTPTR i = 0; i < (pattern_syll_end_size - 1); i++) {
        if (s == pattern_syll_end[i]) {
            res = shift_syll_end[i];
            return res;
       }
    }
    return res;
}




bool can_palat(CFSWString c) {
    if (c.FindOneOf(L"DLNST") > -1) return true;
    return false;
}

CFSWString shift_pattern(CFSWString s) {
    if (s == L'j') return L'j';
    else
        if (s == L'h') return L'h';
    else
        if (s == L'v') return L'v';
    else
        if (s.FindOneOf(L"sS") > -1) return L's';
    else
        if (s.FindOneOf(L"lmnrLN") > -1) return L'L';
    else
        if (s.FindOneOf(L"kptfšT") > -1) return L'Q';
    else
        if (is_vowel(s)) return L'V';
    else
        if (is_consonant(s)) return L'C';
    return s;
}

CFSWString chars_to_phones_part_I(CFSWString &s) {
    /*	müüa -> müia siia?
                    Kuna vältemärgi nihutamise reeglid on ehitatud selliselt, et kohati kasutatakse 
                    foneeme ja kohati ei, siis tuleb täht->foneem teisendus teha kahes jaos.
                    Palataliseerimine põhineb ideel, et palataliseeritud foneemi ümbruses ei saa
                    olla palataliseeruvaid mittepalataliseeritud foneeme :D
	Eritöötlus märkidele:
	[ (ainult C järel), palataliseerime kohe, s.t asendame ümbruses suurtäheliseks
	< 3. välde (ainult V ees) jätame nagu on
	? ebaregulaarne rõhk (ainult V ees) jätame nagu on

	Muud mittetähed välja
     */
    CFSWString res;
    for (INTPTR i = 0; i < s.GetLength(); i++) {
        CFSWString c = s.GetAt(i);
        if (c == L']') {
            CFSWString t = CFSWString(s.GetAt(i - 1)).ToUpper();
            res.SetAt(res.GetLength() - 1, t.GetAt(0));
            //vaatab tagasi juba tehtule; pole kindel, et kas on vajalik
            t = CFSWString(s.GetAt(i - 2)).ToUpper();
            if (can_palat(t)) {
                res.SetAt(res.GetLength() - 2, t.GetAt(0));
                t = CFSWString(s.GetAt(i - 3)).ToUpper();
                if (can_palat(t)) {
                    res.SetAt(res.GetLength() - 2, t.GetAt(0));
                }
            }
            //vaatab ette
            t = CFSWString(s.GetAt(i + 1)).ToUpper();
            if (can_palat(t)) {
                s.SetAt(i + 1, t.GetAt(0));
                t = CFSWString(s.GetAt(i + 2)).ToUpper();
                if (can_palat(t)) {
                    s.SetAt(i + 2, t.GetAt(0));
                }
            }
        } else
            if (c == L'<') {
                res += c;
//            CFSWString t = CFSWString(s.GetAt(i + 1)).ToUpper();
//            s.SetAt(i + 1, t.GetAt(0));
        } else
            if (c == L'?') {
                res += c;
        }
        else
            if (c == L'x') res += L"ks";
        else
            if (c == L'y') res += L"i";
        else
            if (c == L'w') res += L"v";
        else
            if (c == L'z') {
            if (s.GetAt(i + 1) == L'z') {
                s.SetAt(i + 1, L's');
                res += L"t";
            } else
                res += L"s";
        } else
            if (c == L'c') {
            if (s.GetAt(i + 1) == L'e' || s.GetAt(i + 1) == L'i' || s.GetAt(i + 1) == L'<' ) {
                res += L"ts";
            } else
            res += L"k";
        } else
            if (c == L'ü' && is_vowel(s.GetAt(i + 1)) && s.GetAt(i - 1) == L'ü')
            res += L"i";
        else
            if (c == L'q') {
            if (is_vowel(s.GetAt(i + 1))) {
                res += L"k";
                s.SetAt(i + 1, L'v');
            } else
                res += L"k";
        } else
            if (is_char(c)) res += c;
    }
    return res;
}

CFSWString syllabify2(CFSWString s) {
    CFSWString res;
    bool had_vowels = false;

    for (INTPTR i = 0; i < s.GetLength(); i++) {
        CFSWString c = s.GetAt(i);
        CFSWString c_prev = s.GetAt(i-1);
        CFSWString c_next = s.GetAt(i+1);
	// jump over palatalization marker (if still present)
	if (c_next == L']')
	    c_next = s.GetAt(i+2);

        if (is_consonant(c)) {
	    if ( is_vowel(c_next) || (c_next == L'?') || (c_next == L'<') ) {
	    // last consonant starts a new syllable
		if ( had_vowels )
		    // ok, wasnt the beginning of the word
		    // insert dash before it
		    res += d;
	    }
	}
	else
	if ( (c == L'<') && is_vowel(c_prev) ) {
	    // po<eet, zo<oid
	    // insert dash before it
	    res += d;
	}
	else
	if ( (c == L'?') && is_vowel(c_prev) ) {
	    // ego?ismi,
	    // insert dash before it
	    res += d;
	}
	else
	if ( is_vowel(c) && is_vowel(c_prev) && is_vowel(c_next) ) {
	    // poeetiline, viiul
	    // insert dash if c1 c2c2
	    if (c == c_next)
		res += d;
	}

/*
        if (is_consonant(c) && is_vowel(s.GetAt(i - 1)) && is_vowel(s.GetAt(i + 1)))
            res += d;
	// ei silbita "neiu"
        if (is_vowel(c) && is_vowel(s.GetAt(i - 1)) && is_vowel(s.GetAt(i + 1)) && c.ToLower() == s.GetAt(i + 1))
            res += d;
        if (is_consonant(c) && is_consonant(s.GetAt(i - 1)) && is_vowel(s.GetAt(i + 1)) && has_vowel(res)) //küsitav
            res += d;
*/
        res += c;
	if ( (had_vowels == false) && is_vowel(c) )
	    had_vowels = true;
    }
    return res;
}

CFSWString the_shift(CFSWString s) {
    /*
            On mingi võimalus, et lihtsustus tuleb teha kahes astmes. LQ-ta ja LQ-ga (vt shift_pattern). Kõik 
            seotud sellega, et pole	vältenihutusreeglitest lõpuni aru saanud. Eksisteerib Mihkla versioon ja 
            ametlik versioon. Tänud	Mihklale, kes kala asemel annab tattninale õnge, see õpetab ujuma.
            Maadlesin õngega pikalt.
     */

    CFSWString res;
    CFSWString code;
    INTPTR pos;
    INTPTR i = 0;
    INTPTR x;

    // triggered by <, resolved and cleaned by -
    bool code_active = false;

    while (s.GetLength() > 0) {
        CFSWString c = s.GetAt(0);
        s.Delete(0, 1);

        // 3. välte algus
        if (c == L'<') {
            code_active = true;
            pos = i;
	    continue;	// not incrementing i
        }
        // lisatud silbikriips
        else if (c == d && code_active) {
            res += c;
            code += c;

            CFSWString t_code = code;
            t_code += shift_pattern(s.GetAt(0));

            x = pattern_lookup_syll_end(t_code); // orig üle silbipiiri
            if (x > -1) {
                x += pos;
                if (x > res.GetLength()) { // kui kargab järgmisse silpi
                    x = x - res.GetLength();
                    s.Insert(x, colon);
                } else
                    res.Insert(x, colon);
                i++;
            } else {
                x = pattern_lookup_syll_end(code); // orig 
                if (x > -1) {
                    x += pos;
                    res.Insert(x, colon);
                    i++;
                }
            }
            code = empty_str;
	    code_active = false;
        }
        // tavaline täht või '?'
        else {
            res += c;
            if (code_active) {
                code += shift_pattern(c);
            }
        }

        i++;

    } //while

    // sõna lõpus
    if (code_active) {
//        code += L"#";
        //imelik koht ainult "lonksu" pärast
        if ((code.Left(3) == L"VLQ") && ((code.GetAt(3) == L's') || (code.GetAt(3) == L'h') || (code.GetAt(3) == L'v') || (code.GetAt(3) == L'j'))) {
            code = L"VLQC";
        }
        INTPTR x = pattern_lookup_word_end(code);
        if (x > -1) {
            x += pos;
            res.Insert(x, colon);
        }
//        code = empty_str;
    }

    return res;
}

CFSWString word_to_syls(CFSWString word) {
    word = chars_to_phones_part_I(word);
//    CFSWString s = make_char_string(word);
    CFSWString s = syllabify2(word);
    s = the_shift(s);
    return s;
}

bool is_stressed_syl(CFSWString syl) {
    for (INTPTR i = 0; i < syl.GetLength(); i++) {
        if ( (syl.GetAt(i) == colon) || (syl.GetAt(i) == L'?') )
	    return true;
        if ( (is_vowel(syl.GetAt(i))) && (is_vowel(syl.GetAt(i + 1))) )
            return true;
    }
    return false;
}

/*
// et oleks lihtsam 2sid tagasi lülitada ja saada 1 astmlised rõhud

INTPTR extra_stress(CFSArray<syl_struct> &sv, INTPTR size) {
    if (size == 1) return 0;
    else
        for (INTPTR i = 1; i < size; i++)
            for (INTPTR i1 = 0; i1 < sv[i].syl.GetLength(); i1++) {
                if (sv[i].syl.GetAt(i1) == colon) return i;
                if (i1 > 0)
                    if (is_vowel(sv[i].syl.GetAt(i1)) && is_vowel(sv[i].syl.GetAt(i1 - 1)))
                        return i;
            }
    return 0;
}
*/

void add_stress2(CFSArray<syl_struct> &sv, INTPTR wp) {
    /* Kõige radikaalsem rõhutus siiani. 
     * wp = kui on liitsõna esimene liige siis on seal pearõhk.
     */
    INTPTR main_stress = 2;
    INTPTR stress = 1;
    // main is main only in the first member of a compound word
    if (wp > 0)
	main_stress = 1;
    INTPTR size = sv.GetSize();
    bool main_applied = false;

    // just 1 syllable, always stressed
    if (size == 1) {
	    sv[0].stress = main_stress;
    }
    else {
        // init by checking syllables for stressedness
        for (INTPTR i = 0; i < size; i++) {
            if (is_stressed_syl(sv[i].syl)){
                if (main_applied) {
                    sv[i].stress = stress;
                }
                else {
                    sv[i].stress = main_stress;
                    main_applied = true;
                }
            }
        }

	// stress the first syllable if there are no exceptions
	    if (main_applied == false)
	        sv[0].stress = main_stress;

	// fill up to the first applied stress (1st, 3rd etc syllables)
	    for (INTPTR i = 0; i < size-1; i += 2){
	        if ((sv[i].stress == 0) && (sv[i+1].stress == 0)) {
		    sv[i].stress = stress;
	        }
	        else break;
        }

	// for the rest, stress the middle of any remaining 3 consecutive unstressed syllables
	    for (INTPTR i = 0; i < size-2; i++){
	        if ((sv[i].stress == 0) && (sv[i+1].stress == 0) && (sv[i+2].stress == 0)) {
		    sv[i+1].stress = stress;
	        }
        }
    }

    // clean up the last remaining non-phonetical symbol '?'
    for (INTPTR i = 0; i < size; i++) {
	sv[i].syl.Replace(L"?", L"", 1);
    }

/*
    INTPTR stress_type = extra_stress(sv, size);

    if (stress_type == 0) {
        for (INTPTR i = 0; i < size; i++) {
            if (i % 2 == 0) {
                if ((i == 0) && (wp == 0))
                    sv[i].stress = main_stress;
                else
                    sv[i].stress = stress;
            }
        }
        if (size > 1) sv[size - 1].stress = 0;
    }
    else {
        if (wp == 0)
            sv[stress_type].stress = main_stress;
        else
            sv[stress_type].stress = stress;

        //esimene pool
        if (stress_type == 1) sv[0].stress = 0;
        else
            for (INTPTR i = stress_type - 1; i >= 0; i--)
                if (i % 2 == 0)
                    sv[i].stress = stress;

        if ((stress_type % 2 != 0) && (stress_type > 1))
            sv[stress_type - 1].stress = 0;

        //teine pool

        INTPTR lopp = size - stress_type;

        if (lopp > 3) {
            for (INTPTR i = stress_type + 1; i < size; i++)
                if (i % 2 != 0) sv[i].stress = stress;

            sv[size - 1].stress = 0;
        }
    }
*/
}

void do_syls(word_struct &w) {
    CFSArray<syl_struct> sv, sv_temp;
    syl_struct ss;
    CFSArray<CFSWString> temp_arr, c_words;
    ss.phone_c = 0, ss.word_p = 0, ss.phr_p = 0, ss.utt_p = 0;
    w.syl_c = 0;
    INTPTR word_p = 1;

    explode(w.mi.m_szRoot, L"_", c_words);

    for (INTPTR cw = 0; cw < c_words.GetSize(); cw++) {

        CFSWString s = word_to_syls(c_words[cw]);

        //MINGI MUSTRITE ERROR paindliKkus
        s.Replace(L"K", L"k", 1);
        s.Replace(L"R", L"r", 1);
        s.Replace(L"V", L"v", 1);

        explode(s, d, temp_arr);
        ss.stress = 0;
        sv_temp.Cleanup();

        for (INTPTR i = 0; i < temp_arr.GetSize(); i++) {
            ss.syl = temp_arr[i];
            ss.stress = 0; // rõhu algväärtus
            ss.word_p = word_p++;
            sv_temp.AddItem(ss);
        }

        add_stress2(sv_temp, cw);

        for (INTPTR i = 0; i < sv_temp.GetSize(); i++)
            sv.AddItem(sv_temp[i]);
    }

    w.syl_vector = sv;

}



