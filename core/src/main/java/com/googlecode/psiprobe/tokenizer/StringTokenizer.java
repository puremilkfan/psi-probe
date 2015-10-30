/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */

package com.googlecode.psiprobe.tokenizer;

import java.io.IOException;
import java.io.StringReader;

/**
 * The Class StringTokenizer.
 *
 * @author Vlad Ilyushchenko
 */
public class StringTokenizer extends Tokenizer {

  /**
   * Instantiates a new string tokenizer.
   */
  public StringTokenizer() {}

  /**
   * Instantiates a new string tokenizer.
   *
   * @param str the str
   */
  public StringTokenizer(final String str) {
    setString(str);
  }

  /**
   * Sets the string.
   *
   * @param str the new string
   */
  public void setString(final String str) {
    setReader(new StringReader(str));
  }

  @Override
  public boolean hasMore() {
    try {
      return super.hasMore();
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public Token getToken() {
    try {
      return super.getToken();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public Token nextToken() {
    try {
      return super.nextToken();
    } catch (IOException e) {
      return null;
    }
  }

}
