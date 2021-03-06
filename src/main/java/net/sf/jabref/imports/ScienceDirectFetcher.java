/*  Copyright (C) 2003-2011 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.imports;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.OutputPrinter;
import net.sf.jabref.net.URLDownload;

import javax.swing.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScienceDirectFetcher implements EntryFetcher {

    private static final int MAX_PAGES_TO_LOAD = 8;
    private static final String WEBSITE_URL = "http://www.sciencedirect.com";
    private static final String SEARCH_URL = ScienceDirectFetcher.WEBSITE_URL + "/science/quicksearch?query=";

    private static final String linkPrefix = "http://www.sciencedirect.com/science?_ob=ArticleURL&";
    private static final Pattern linkPattern = Pattern.compile(
            "<a href=\"" +
                    ScienceDirectFetcher.linkPrefix.replaceAll("\\?", "\\\\?") +
                    "([^\"]+)\"\"");

    protected static final Pattern nextPagePattern = Pattern.compile(
            "<a href=\"(.*)\">Next &gt;");

    private boolean stopFetching = false;


    @Override
    public String getHelpPage() {
        return "ScienceDirect.html";
    }

    @Override
    public String getKeyName() {
        return "ScienceDirect";
    }

    @Override
    public JPanel getOptionsPanel() {
        // No Options panel
        return null;
    }

    @Override
    public String getTitle() {
        return Globals.menuTitle("Search ScienceDirect");
    }

    @Override
    public void stopFetching() {
        stopFetching = true;
        boolean noAccessFound = false;
    }

    @Override
    public boolean processQuery(String query, ImportInspector dialog, OutputPrinter status) {
        stopFetching = false;
        try {
            List<String> citations = getCitations(query);
            if (citations == null) {
                return false;
            }
            if (citations.size() == 0) {
                status.showMessage(Globals.lang("No entries found for the search string '%0'",
                        query),
                        Globals.lang("Search ScienceDirect"), JOptionPane.INFORMATION_MESSAGE);
                return false;
            }

            int i = 0;
            for (String cit : citations) {
                if (stopFetching) {
                    break;
                }
                BibtexEntry entry = BibsonomyScraper.getEntry(cit);
                if (entry != null) {
                    dialog.addEntry(entry);
                }
                dialog.setProgress(++i, citations.size());
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            status.showMessage(Globals.lang("Error while fetching from ScienceDirect") + ": " + e.getMessage());
        }
        return false;
    }

    /**
     *
     * @param query
     *            The search term to query JStor for.
     * @return a list of IDs
     * @throws java.io.IOException
     */
    private List<String> getCitations(String query) throws IOException {
        String urlQuery;
        ArrayList<String> ids = new ArrayList<String>();
        try {
            urlQuery = ScienceDirectFetcher.SEARCH_URL + URLEncoder.encode(query, "UTF-8");
            int count = 1;
            String nextPage;
            while (((nextPage = getCitationsFromUrl(urlQuery, ids)) != null)
                    && (count < ScienceDirectFetcher.MAX_PAGES_TO_LOAD)) {
                urlQuery = nextPage;
                count++;
            }
            return ids;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCitationsFromUrl(String urlQuery, List<String> ids) throws IOException {
        URL url = new URL(urlQuery);
        String cont = new URLDownload(url).downloadToString();
        //String entirePage = cont;
        Matcher m = ScienceDirectFetcher.linkPattern.matcher(cont);
        if (m.find()) {
            while (m.find()) {
                ids.add(ScienceDirectFetcher.linkPrefix + m.group(1));
                cont = cont.substring(m.end());
                m = ScienceDirectFetcher.linkPattern.matcher(cont);
            }
        }

        else {
            return null;
        }
        /*m = nextPagePattern.matcher(entirePage);
        if (m.find()) {
            String newQuery = WEBSITE_URL +m.group(1);
            return newQuery;
        }
        else*/
        return null;
    }

}
