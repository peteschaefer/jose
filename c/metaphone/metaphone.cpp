/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002,2003 Peter Schäfer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */


#ifdef __WIN__
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif

#include <string.h>
#include <my_global.h>
#include <my_sys.h>
#include <mysql.h>

/*
	String jucase(String)

			similar to ucase(String), but non-letter characters are replaced by spaces
			leading and trailing non-letters are trimmed

	String metaphone(String)

			the well known double Metaphone soundex algorithm
 */

extern "C" {

	EXPORT my_bool jucase_init(UDF_INIT *initid, UDF_ARGS *args, char *message);

	EXPORT void jucase_deinit(UDF_INIT *initid);

	EXPORT char *jucase(UDF_INIT *initid, UDF_ARGS *args, char *result,
	       unsigned long *length, char *is_null, char *error);


	EXPORT my_bool metaphone_init(UDF_INIT *initid, UDF_ARGS *args, char *message);

	EXPORT void metaphone_deinit(UDF_INIT *initid);

	EXPORT char *metaphone(UDF_INIT *initid, UDF_ARGS *args, char *result,
	       unsigned long *length, char *is_null, char *error);

}

#include "char_maps.c"

#define IS_LETTER(c)	isLetter[(unsigned char)c]
#define STRIP(c)		stripDiacritics[(unsigned char)c]
#define TO_UPPER(c)		stripDiacriticsToUpper[(unsigned char)c]

/****************************************************************
 *		jucase
 ****************************************************************/

#define UCASE_MAX 255

my_bool jucase_init(UDF_INIT *initid, UDF_ARGS *args, char *message)
{
  if (args->arg_count != 1 || args->arg_type[0] != STRING_RESULT)
  {
    strcpy(message,"Wrong arguments to metaphon;  Use the source");
    return 1;
  }

  initid->maybe_null = 1;
  initid->max_length = UCASE_MAX;
  
  return 0;
}

void jucase_deinit(UDF_INIT *initid)
{
}

char *jucase(UDF_INIT *initid, UDF_ARGS *args, char *result,
	       unsigned long *length, char *is_null, char *error)
{
		const char *word = args->args[0];
		if (word==NULL) {					// Null argument
			*is_null=1;
			return NULL;
		}
		
		int len = args->lengths[0];
		if (len > UCASE_MAX) len = UCASE_MAX;

		char* res = result;
		const char* end = word+len;

		//		trim leading blanks
		while (word < end && !IS_LETTER(*word))
				word++;

		//		trim trailing blanks
		while (end > word && !IS_LETTER(*(end-1)))
				end--;

		//		copy
		while (word < end) {
				if (IS_LETTER(*word)) {
						*res++ = TO_UPPER(*word++);
				}
				else {
						*res++ = ' ';
						//		skip remaining
						while ((++word < end) && !IS_LETTER(*word))
								;
				}
		}
		
		*length = (res-result);
		return result;
}


/****************************************************************
 *		metaphon
 ****************************************************************/

#define METAPHONE_MAX 6
#define ARG_ALLOC (2*METAPHONE_MAX)

my_bool metaphone_init(UDF_INIT *initid, UDF_ARGS *args, char *message)
{
  if (args->arg_count != 1 || args->arg_type[0] != STRING_RESULT)
  {
    strcpy(message,"Wrong arguments to metaphon;  Use the source");
    return 1;
  }
  initid->maybe_null = 1;
  initid->max_length = METAPHONE_MAX+4;
  return 0;
}


void metaphone_deinit(UDF_INIT *initid)
{
}

void toUpper(char* dst, char* src, int len);

bool isfrontv(char c);
bool isvowel(char c);
bool isvarson(char c);

bool startsWith(char* c, char* end, char* comp);

char *metaphone (UDF_INIT *initid, UDF_ARGS *args, char *result,
	       unsigned long *length, char *is_null, char *error)
{
		if (args->args[0]==NULL) {
				*is_null = 1;
				return NULL;
		}

		int len = args->lengths[0];		
		if (len==0) {	//		return empty string
				*length = 0;
				return result;
		}
		if (len==1) {	//		single character is itself
				*length = 1;
				*result = TO_UPPER(args->args[0][0]);
				return result;
		}
		if (len > ARG_ALLOC) len = ARG_ALLOC;

		char temp[ARG_ALLOC];
		toUpper(temp,args->args[0],len);

		char* start = temp;
		char* end = temp+len;
		char* code = result;
		char* codend = code+METAPHONE_MAX;

		bool hard = false;
		
	  // handle initial 2 characters exceptions
      switch (*start)
      {
		case 'K': case 'G' : case 'P' : /* looking for KN, etc*/
		  if (start[1] == 'N') start++;
		  break;
		case 'A': /* looking for AE */
		  if (start[1] == 'E') start++;
		  break;
		case 'W' : /* looking for WR or WH */
		  if (start[1] == 'R')   // WR -> R
		    start++;
		  else if (start[1] == 'H')
			*++start = 'W';		// WH -> W
		  break;
		case 'X' : /* initial X becomes S */
		  *start = 'S';
		  break ;
      } // now initials fixed



	  char* n = start;
      while((code < codend) && // max code size of 4 works well
            (n < end))
      {
        char symb = *n;
        // remove duplicate letters except C
        if ((symb != 'C') &&
           (n > start) && (n[-1] == symb))
        {
	        n++;
	        continue;
        }

         switch( symb )
         {
            case 'A' : case 'E' : case 'I' : case 'O' : case 'U' :
              if (n == start) *code++ = symb;
              break ; // only use vowel if leading char

            case 'B' :
              if ((n > start) &&
                  !(n+1 == end) && // not MB at end of word
                  (n[-1] == 'M'))
                  *code++ = symb;
              else
	              *code++ = symb;  //  TODO this makes no sense
              break ;

            case 'C' : // lots of C special cases
              /* discard if SCI, SCE or SCY */
              if( ( n > 0 ) &&
                  ( n[-1] == 'S' ) &&
                  ( n + 1 < end ) &&
                  isfrontv(n[1]))
                break;

	         if( startsWith(n,end, "CIA")) { // "CIA" -> X
                 *code++ = 'X';
		         break;
	         }
	         if( ( n + 1 < end ) &&
                  isfrontv(n[1])) {
                *code++ = 'S';  // CI,CE,CY -> S
                break;
             }
             if(( n > start) &&
                 startsWith(n-1,end,"SCH")) { // SCH->sk
                 *code++ = 'K';
	             break;
             }
             if( startsWith(n,end,"CH"))
              { // detect CH
                if((n == start) &&
                   (end-start >= 3) &&    // CH consonant -> K consonant
                   ! isvowel(start[2]))
                     *code++ = 'K';
                else
	                *code++ = 'X'; // CHvowel -> X
              }
              else
	              *code++ = 'K';
             break;

            case 'D' :
              if(( n + 2 < end )&&  // DGE DGI DGY -> J
                 ( n[1] == 'G' )&&
                 isfrontv(n[2]))
              {
                  *code++ = 'J';
	              n += 2 ;
              }
              else
	              *code++ = 'T';
              break;

            case 'G' : // GH silent at end or before consonant
              if(( n + 2 == end )&&
                 (n[1] == 'H' ))
	              break;
              if(( n + 2 < end ) &&
                 (n[1] == 'H' )&&
                 ! isvowel(n[2]))
	             break;

              if((n > start) &&
                 startsWith(n,end,"GN")||
                 startsWith(n,end,"GNED"))
	              break; // silent G

              if(( n > 0 ) &&
                 (n[-1] == 'G')) hard = true;
              else
	              hard = false;

              if((n+1 < end) &&
                 isfrontv(n[1])&&
                 (!hard) )
	              *code++ = 'J';
              else
	              *code++ = 'K';
              break ;

            case 'H':
              if( n + 1 == end )
	              break; // terminal H
              if((n > 0) &&
                 isvarson(n[-1]))
	              break;

              if( isvowel(n[1]))
                  *code++ = 'H';// Hvowel
              break;

            case 'F': case 'J' : case 'L' :
            case 'M': case 'N' : case 'R' :
              *code++ = symb;
	          break ;

            case 'K' :
              if( n > start) { // not initial
                if( n[-1] != 'C' )
                     *code++ = symb;
              }
              else
	              *code++ = symb; // initial K
              break ;

            case 'P' :
              if((n + 1 < end) &&  // PH -> F
                 (n[1] == 'H'))
	              *code++ = 'F';
              else
	              *code++ = symb;
              break ;

            case 'Q' :
             *code++ = 'K';
	         break;

            case 'S' :

              if(startsWith(n,end,"SH") ||
                 startsWith(n,end,"SIO") ||
                 startsWith(n,end,"SIA"))
	              *code++ = 'X';
              else
	              *code++ = 'S';
              break ;

            case 'T' :
              if(startsWith(n,end,"TIA") ||
                 startsWith(n,end,"TIO") )
              {
                    *code++ = 'X'; break;
              }

	         if( startsWith(n,end,"TCH"))
		         break;

              // substitute numeral 0 for TH (resembles theta after all)
              if( startsWith(n,end,"TH"))
	              *code++ = '0';     //  ZERO (not OH)
              else
	              *code++ = 'T';
              break ;

            case 'V' :
              *code++ = 'F';
	          break ;

            case 'W' : case 'Y' : // silent if not followed by vowel
              if((n+1 < end) &&
                 isvowel(n[1]))
                   *code++ = symb;
              break ;

            case 'X' :
	         *code++ = 'K';
	         *code++ = 'S';
             break ;

            case 'Z' :
             *code++ = 'S';
	         break ;
          } // end switch

          n++;
      }	  


		if (code > codend) code = codend;
		*length = (code-result);
		return result;
}


void toUpper(char* dst, char* src, int len)
{
	while (len-- > 0)
		*dst++ = TO_UPPER(*src++);
}

bool startsWith(char* word, char* end, char* comp)
{
	while (word < end && *comp)
		if (*word++ != *comp++) return false;
    return (*comp)==0;
}

bool isfrontv(char c)
{
	return (c=='E') || (c=='I') || (c=='Y');
}

bool isvowel(char c)
{
	return (c=='A') || (c=='E') || (c=='I') || (c=='O') || (c=='U');
}

bool isvarson(char c)
{
	return (c=='C') || (c=='S') || (c=='P') || (c=='T') || (c=='G');
}
