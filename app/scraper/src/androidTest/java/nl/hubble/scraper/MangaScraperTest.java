package nl.hubble.scraper;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MangaScraperTest {
    private static MangaScraper scraper;

    @BeforeClass
    public static void getContext() throws IOException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Utils.Parse.resetQueries(appContext);
        scraper = new MangaScraper(appContext);
    }

    @Test
    public void testMangadex() {
        Manga m = parse("https://api.mangadex.org/manga/136a8c13-cf72-427a-8d76-23f1bf070c0e");
        m.setCover("test");
        m.setAuthors(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testLHTranslation() {
        parseAndCheck("https://lhtranslation.net/manga-argate-online.html/");
    }

    @Test
    public void testMangakakalot() {
        parseAndCheck("https://mangakakalot.com/read-qh2yl158504879737");
    }

    @Test
    public void testMangakakalots() {
        parseAndCheck("https://ww2.mangakakalots.com/manga/hq918132");
    }

    @Test
    public void testMangaNelo() {
        parseAndCheck("https://manganelo.com/manga/ma924373");
    }

    @Test
    public void testManganato() {
        Manga m = parse("https://readmanganato.com/manga-lb989110");
        m.setAltTitles(List.of("test"));
        Log.e("OWO", "testManganato: " + m.getChapters().get(0).getPosted());
        shouldBeComplete(m);
    }

    @Test
    public void testMangaBat() {
        parseAndCheck("https://m.mangabat.com/read-nh391726");
    }

    @Test
    public void testKissManga() {
        Manga m = parse("http://kissmanga.nl/manga/the-eunuchs-consort-rules-the-world");
        m.setAltTitles(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testMangaBatBest() {
        Manga m = parse("http://mangabat.best/manga/my-little-baby-prince");
        m.setAltTitles(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testIsekaiScan() {
        Manga m = parse("https://isekaiscan.com/manga/gusha-no-hoshi/");
        m.setAltTitles(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testZeroScans() {
        Manga m = parse("https://zeroscans.com/comics/636122-hero-i-quit-a-long-time-ago");
        m.setAltTitles(List.of("test"));
        m.setAuthors(List.of("test"));
        m.setGenres(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testTheNonames() {
        Manga m = parse("https://the-nonames.com/comics/241705-danshi-koukousei-wo-yashinaitai-onee-san-no-hanashi");
        m.setAltTitles(List.of("test"));
        m.setAuthors(List.of("test"));
        m.setGenres(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testReaperScans() {
        Manga m = parse("https://reaperscans.com/comics/777794-demonic-emperor");
        m.setAltTitles(List.of("test"));
        m.setAuthors(List.of("test"));
        m.setGenres(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testMangaStream() {
        Manga m = parse("http://mangastream.mobi/manga/solo-leveling");
        m.setAltTitles(List.of("test"));
        m.setAuthors(List.of("test"));
        m.setGenres(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testMangaFreak() {
        parseAndCheck("http://mangafreak.cloud/manga/kimetsu-no-yaiba");
    }

    @Test
    public void testMangaRockTeamSite() {
        parseAndCheck("http://mangarockteam.site/manga/beast-worlds-wild-consort");
    }

    @Test
    public void testWhimsubs() {
        Manga m = parse("https://whimsubs.xyz/r/series/shikkaku-kara-hajimeru-nariagari-madou-shidou/");
        m.setDescription("test");
        m.setAltTitles(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testMngDoom() {
        parseAndCheck("https://www.mngdoom.com/star-martial-god-technique");
    }

    @Test
    public void testMangaInn() {
        parseAndCheck("https://www.mangainn.net/ore-no-ie-ga-maryoku-spot-datta-ken-sundeiru-dake-de-sekai-saikyou");
    }

    @Test
    public void testMangaFast() {
        Manga m = parse("https://mangafast.net/read/the-beginning-after-the-end/");
        m.setAltTitles(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testReadM() {
        parseAndCheck("https://readm.org/manga/16539");
    }

    @Test
    public void testMangaKik() {
        parseAndCheck("https://mangakik.com/manga/magic-emperor/");
    }

    @Test
    public void testManhuaus() {
        parseAndCheck("https://manhuaus.com/manga/magic-emperor/");
    }

    @Test
    public void testMangaWeebs() {
        parseAndCheck("https://mangaweebs.in/manga/i-raised-the-beast-well/");
    }

    @Test
    public void testIsekaiScanManga() {
        Manga m = parse("https://isekaiscanmanga.com/manga/magic-emperor/");
        m.setAltTitles(List.of("test"));
        shouldBeComplete(m);
    }

    @Test
    public void testManhuaPlus() {
        parseAndCheck("https://manhuaplus.com/manga/the-cultivators-immortal-is-my-sister/");
    }

    @Test
    public void testMangaSushi() {
        parseAndCheck("https://mangasushi.net/manga/yuukyuu-no-gusha-asley-no-kenja-no-susume/");
    }

    @Test
    public void test1stKissManga() {
        parseAndCheck("https://1stkissmanga.com/manga/magic-emperor/");
    }

    @Test
    public void testMangaFoxFull() {
        parseAndCheck("https://mangafoxfull.com/manga/magic-emperor/");
    }

    @Test
    public void testS2Manga() {
        parseAndCheck("https://s2manga.com/manga/under-the-oak-tree/");
    }

    @Test
    public void testManhwaTop() {
        parseAndCheck("https://manhwatop.com/manga/magic-emperor/");
    }

    @Test
    public void testManga68() {
        parseAndCheck("https://manga68.com/manga/magic-emperor/");
    }

    @Test
    public void testManga347() {
        parseAndCheck("https://manga347.com/manga/magic-emperor/");
    }

    @Test
    public void testMixedManga() {
        parseAndCheck("https://mixedmanga.com/manga/the-eunuchs-consort-rules-the-world/");
    }

    @Test
    public void testMangaHZ() {
        parseAndCheck("https://mangahz.com/read/the-eunuchs-consort-rules-the-world/");
    }

    @Test
    public void testManhuaDex() {
        parseAndCheck("https://manhuadex.com/manhua/the-eunuchs-consort-rules-the-world/");
    }

    @Test
    public void testMangaChill() {
        parseAndCheck("https://mangachill.com/manga/the-eunuchs-consort-rules-the-world/");
    }

    @Test
    public void testMangaRockTeam() {
        parseAndCheck("https://mangarockteam.com/manga/the-eunuchs-consort-rules-the-world/");
    }

    @Test
    public void testMangaZukiTeam() {
        parseAndCheck("https://mangazukiteam.com/manga/the-eunuchs-consort-rules-the-world/");
    }

    @Test
    public void testAZManhwa() {
        parseAndCheck("https://azmanhwa.net/manga/the-eunuchs-consort-rules-the-world/");
    }

    @Test
    public void testTopManhua() {
        parseAndCheck("https://topmanhua.com/manga/lightning-degree/");
    }

    @Test
    public void testMangaFunny() {
        parseAndCheck("https://mangafunny.com/manga/past-lives-of-the-thunder-god/");
    }

    @Test
    public void testYaoiMobi() {
        parseAndCheck("https://yaoi.mobi/manga/stack-overflow-raw-yaoi0003/");
    }

    @Test
    public void testMangaTX() {
        parseAndCheck("https://mangatx.com/manga/lightning-degree/");
    }

    public Manga parse(String url) {
        try {
            return scraper.parse(new URL(url));
        } catch (Exception e) {
            Log.e("OWO", "parse: ", e);
            fail(e.getMessage());
        }
        return null;
    }

    public void parseAndCheck(String url) {
        shouldBeComplete(parse(url));
    }

    private void shouldBeComplete(Manga manga) {
        assertNotNull("manga", manga);
        assertNotNull("href", manga.getHref());
        assertNotNull("title", manga.getTitle());
        assertNotNull("alt_titles", manga.getAltTitles());
        assertNotNull("authors", manga.getAuthors());
        assertNotNull("chapters", manga.getChapters());
        assertNotNull("cover", manga.getCover());
        assertNotNull("description", manga.getDescription());
        assertNotNull("genres", manga.getGenres());
        assertNotNull("hostname", manga.getHostname());

        assertNotEquals("href", manga.getHref(), "");
        assertNotEquals("title", manga.getTitle(), "");
        assertNotEquals("cover", manga.getCover(), "");
        assertNotEquals("description", manga.getDescription(), "");
        assertNotEquals("hostname", manga.getHostname(), "");
        assertFalse("alt_titles", manga.getAltTitles().isEmpty());
        assertFalse("authors", manga.getAuthors().isEmpty());
        assertFalse("chapters", manga.getChapters().isEmpty());
        assertFalse("genres" + manga.getGenres(), manga.getGenres().isEmpty());

        for (Chapter chapter : manga.getChapters()) {
            assertNotNull("chapter", chapter);
            assertNotNull("href", chapter.getHref());
            assertNotNull("title", chapter.getTitle());
            assertNotEquals("number", chapter.getNumber(), 0);
            assertNotEquals("posted", chapter.getPosted(), 0);
        }

        checkImages(manga.getChapters().get(0));
    }

    public void checkImages(Chapter chapter) {
        try {
            List<String> images = scraper.images(new URL(chapter.getHref()));
            assertFalse(chapter.getHref(), images.isEmpty());
        } catch (Exception e) {
            fail(chapter.getHref() + ": " + e.getMessage());
        }
    }
}