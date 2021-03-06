package net.sf.jabref.export.layout;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.JabRefPreferences;
import net.sf.jabref.imports.BibtexParser;
import net.sf.jabref.imports.ParserResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class LayoutTest {

    /**
     * Initialize Preferences.
     */
    @Before
    public void setUp() throws Exception {
        if (Globals.prefs == null) {
            Globals.prefs = JabRefPreferences.getInstance();
        }
    }

    /**
     * Return Test data.
     */
    public String t1BibtexString() {
        return "@article{canh05,\n"
                + "  author = {This\nis\na\ntext},\n"
                + "  title = {Effective work practices for floss development: A model and propositions},\n"
                + "  booktitle = {Hawaii International Conference On System Sciences (HICSS)},\n"
                + "  year = {2005},\n" + "  owner = {oezbek},\n" + "  timestamp = {2006.05.29},\n"
                + "  url = {http://james.howison.name/publications.html},\n" + "  abstract = {\\~{n}\n"
                + "\\~n\n" + "\\'i\n" + "\\i\n" + "\\i}\n" + "}\n";
    }

    public static BibtexEntry bibtexString2BibtexEntry(String s) throws IOException {
        ParserResult result = BibtexParser.parse(new StringReader(s));
        Collection<BibtexEntry> c = result.getDatabase().getEntries();
        Assert.assertEquals(1, c.size());
        return c.iterator().next();
    }

    public String layout(String layoutFile, String entry) throws Exception {

        BibtexEntry be = LayoutTest.bibtexString2BibtexEntry(entry);
        StringReader sr = new StringReader(layoutFile.replaceAll("__NEWLINE__", "\n"));
        Layout layout = new LayoutHelper(sr).getLayoutFromText(Globals.FORMATTER_PACKAGE);

        return layout.doLayout(be, null);
    }

    @Test
    public void testLayoutBibtextype() throws Exception {
        Assert.assertEquals("Other", layout("\\bibtextype", "@other{bla, author={This\nis\na\ntext}}"));
        Assert.assertEquals("Article", layout("\\bibtextype", "@article{bla, author={This\nis\na\ntext}}"));
        Assert.assertEquals("Misc", layout("\\bibtextype", "@misc{bla, author={This\nis\na\ntext}}"));
    }

    @Test
    @Ignore
    public void testHTMLChar() throws Exception {
        String layoutText = layout("\\begin{author}\\format[HTMLChars]{\\author}\\end{author} ",
                "@other{bla, author={This\nis\na\ntext}}");

        Assert.assertEquals("This is a text ", layoutText);

        layoutText = layout("\\begin{author}\\format[HTMLChars]{\\author}\\end{author}",
                "@other{bla, author={This\nis\na\ntext}}");

        Assert.assertEquals("This is a text", layoutText);

        layoutText = layout("\\begin{author}\\format[HTMLChars]{\\author}\\end{author} ",
                "@other{bla, author={This\nis\na\n\ntext}}");

        Assert.assertEquals("This is a<br>text ", layoutText);
    }

    @Test
    public void testPluginLoading() throws Exception {
        String layoutText = layout("\\begin{author}\\format[NameFormatter]{\\author}\\end{author}",
                "@other{bla, author={Joe Doe and Jane, Moon}}");

        Assert.assertEquals("Joe Doe, Moon Jane", layoutText);
    }

    /**
     * [ 1495181 ] Dotless i and tilde not handled in preview
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testLayout() throws Exception {

        String layoutText = layout(
                "<font face=\"arial\">\\begin{abstract}<BR><BR><b>Abstract: </b> \\format[HTMLChars]{\\abstract}\\end{abstract}</font>",
                t1BibtexString());

        Assert.assertEquals(
                "<font face=\"arial\"><BR><BR><b>Abstract: </b> &ntilde; &ntilde; &iacute; &#305; &#305;</font>",
                layoutText);
    }
}
