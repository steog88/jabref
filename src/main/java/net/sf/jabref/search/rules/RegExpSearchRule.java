/*
 Copyright (C) 2003 Nathan Dunn, Morten O. Alver

 All programs in this directory and
 subdirectories are published under the GNU General Public License as
 described below.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or (at
 your option) any later version.

 This program is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 USA

 Further information about the GNU GPL is available at:
 http://www.gnu.org/copyleft/gpl.ja.html

 */
package net.sf.jabref.search.rules;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.export.layout.format.RemoveLatexCommands;
import net.sf.jabref.search.SearchRule;

public class RegExpSearchRule implements SearchRule {

    private static final RemoveLatexCommands removeBrackets = new RemoveLatexCommands();

    private final boolean caseSensitive;

    public RegExpSearchRule(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean validateSearchStrings(String query) {
        try {
            compileSearchString(query);
        } catch (PatternSyntaxException e) {
            return false;
        }
        return true;
    }

    private Pattern compileSearchString(String searchString) {
        int flags = 0;
        if (!caseSensitive) {
            flags = Pattern.CASE_INSENSITIVE; // testing
        }

        return Pattern.compile(searchString, flags);
    }

    @Override
    public int applyRule(String query, BibtexEntry bibtexEntry) throws PatternSyntaxException {

        int score = 0;

        Pattern pattern = compileSearchString(query);

        score += searchFields(bibtexEntry.getAllFields(), bibtexEntry, pattern);

        return score;
    }

    private int searchFields(Set<String> fields, BibtexEntry bibtexEntry, Pattern pattern) {
        if (fields == null) {
            return 0;
        }

        int score = 0;
        for (String field : fields) {
            try {
                Object value = bibtexEntry.getField(field);
                if (value != null) {
                    Matcher m = pattern.matcher(RegExpSearchRule.removeBrackets.format((String) value));
                    if (m.find()) {
                        score++;
                    }
                }
            } catch (Throwable t) {
                System.err.println("Searching error: " + t);
            }
        }
        return score;
    }

}
