//
// Created by Valentin Platzgummer on 27.02.18.
//

/** Directory read test
 *
 *  @author: Valentin Platzgummer
 *  @date: 27.02.18
 *  @brief: output the current working directory as an absolute path
 */

#include <stdio.h>
#include <stdlib.h> /* for free */
#include <unistd.h> /* for getced() */

int main (int argc, char ** argv)
{
    char *returnChar = NULL;

    /* The getcwd function returns an absolute file name representing the current working directory, storing it in the character array buffer that you provide.
     * The size argument is how you tell the system the allocation size of buffer.
     * The GNU C Library version of this function also permits you to specify a null pointer for the buffer argument. Then getcwd allocates a buffer automatically, as with malloc (see Unconstrained Allocation).
     * If the size is greater than zero, then the buffer is that large; otherwise, the buffer is as large as necessary to hold the result.
    */
    returnChar = getcwd(NULL, 0);

    printf("Current directory: %s\n", returnChar);
    free(returnChar);
    return 0;
}