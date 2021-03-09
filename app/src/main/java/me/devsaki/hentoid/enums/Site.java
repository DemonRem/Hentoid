package me.devsaki.hentoid.enums;

import androidx.annotation.Nullable;

import io.objectbox.converter.PropertyConverter;
import me.devsaki.hentoid.R;
import me.devsaki.hentoid.util.network.HttpHelper;
import timber.log.Timber;

/**
 * Created by neko on 20/06/2015.
 * Site enumerator
 */
public enum Site {

    // NOTE : to maintain compatiblity with saved JSON files and prefs, do _not_ edit either existing names or codes
    FAKKU(0, "Fakku", "https://www.fakku.net", R.drawable.ic_menu_fakku), // Legacy support for old fakku archives
    PURURIN(1, "Pururin", "https://pururin.io", R.drawable.ic_menu_pururin),
    HITOMI(2, "hitomi", "https://hitomi.la", R.drawable.ic_menu_hitomi, false, false, true, false, false, false), // Hitomi needs a desktop agent to properly display gallery images on some devices
    NHENTAI(3, "nhentai", "https://nhentai.net", R.drawable.ic_menu_nhentai, true, true, true, false, false, false),
    TSUMINO(4, "tsumino", "https://www.tsumino.com", R.drawable.ic_menu_tsumino, true, true, true, false, false, false),
    HENTAICAFE(5, "hentaicafe", "https://hentai.cafe", R.drawable.ic_menu_hentaicafe, true, true, true, false, false, false),
    ASMHENTAI(6, "asmhentai", "https://asmhentai.com", R.drawable.ic_menu_asmhentai, true, true, true, false, false, false),
    ASMHENTAI_COMICS(7, "asmhentai comics", "https://comics.asmhentai.com", R.drawable.ic_menu_asmcomics, true, true, true, false, false, false),
    EHENTAI(8, "e-hentai", "https://e-hentai.org", R.drawable.ic_menu_ehentai, true, true, true, false, true, false),
    FAKKU2(9, "Fakku", "https://www.fakku.net", R.drawable.ic_menu_fakku, true, false, false, true, false, false), // Post-processing + doesn't use webview
    NEXUS(10, "Hentai Nexus", "https://hentainexus.com", R.drawable.ic_menu_nexus),
    MUSES(11, "8Muses", "https://www.8muses.com", R.drawable.ic_menu_8muses),
    DOUJINS(12, "doujins.com", "https://doujins.com/", R.drawable.ic_menu_doujins),
    LUSCIOUS(13, "luscious.net", "https://members.luscious.net/manga/", R.drawable.ic_menu_luscious),
    EXHENTAI(14, "exhentai", "https://exhentai.org", R.drawable.ic_menu_exhentai, true, false, true, false, true, false),
    PORNCOMIX(15, "porncomixonline", "https://www.porncomixonline.net/", R.drawable.ic_menu_porncomix),
    HBROWSE(16, "Hbrowse", "https://www.hbrowse.com/", R.drawable.ic_menu_hbrowse),
    HENTAI2READ(17, "Hentai2Read", "https://hentai2read.com/", R.drawable.ic_menu_hentai2read),
    HENTAIFOX(18, "Hentaifox", "https://hentaifox.com", R.drawable.ic_menu_hentaifox),
    MRM(19, "MyReadingManga", "https://myreadingmanga.info/", R.drawable.ic_menu_mrm),
    MANHWA(20, "ManwhaHentai", "https://manhwahentai.me/", R.drawable.ic_menu_manhwa, true, false, true, false, false, true), // Cover-based page updates
    IMHENTAI(21, "Imhentai", "https://imhentai.xxx", R.drawable.ic_menu_imhentai),
    TOONILY(22, "Toonily", "https://toonily.com/", R.drawable.ic_menu_toonily, true, false, true, false, false, true), // Cover-based page updates
    NONE(98, "none", "", R.drawable.ic_external_library), // External library; fallback site
    PANDA(99, "panda", "https://www.mangapanda.com", R.drawable.ic_menu_panda); // Safe-for-work/wife/gf option; not used anymore and kept here for retrocompatibility


    private static final Site[] INVISIBLE_SITES = {
            HENTAICAFE, // Removed as per Fakku request
            FAKKU, // Old Fakku; kept for retrocompatibility
            ASMHENTAI_COMICS, // Does not work directly
            PANDA, // Dropped; kept for retrocompatibility
            NONE // Technical fallback
    };


    private final int code;
    private final String description;
    private final String url;
    private final int ico;
    private final boolean useMobileAgent;
    private final boolean useHentoidAgent;
    private final boolean useWebviewAgent;
    private final boolean hasImageProcessing;
    private final boolean hasBackupURLs;
    private final boolean hasCoverBasedPageUpdates;

    Site(int code,
         String description,
         String url,
         int ico,
         boolean useMobileAgent,
         boolean useHentoidAgent,
         boolean useWebviewAgent,
         boolean hasImageProcessing,
         boolean hasBackupURLs,
         boolean hasCoverBasedPageUpdates) {
        this.code = code;
        this.description = description;
        this.url = url;
        this.ico = ico;
        this.useMobileAgent = useMobileAgent;
        this.useHentoidAgent = useHentoidAgent;
        this.useWebviewAgent = useWebviewAgent;
        this.hasImageProcessing = hasImageProcessing;
        this.hasBackupURLs = hasBackupURLs;
        this.hasCoverBasedPageUpdates = hasCoverBasedPageUpdates;
    }

    Site(int code,
         String description,
         String url,
         int ico) {
        this.code = code;
        this.description = description;
        this.url = url;
        this.ico = ico;
        this.useMobileAgent = true;
        this.useHentoidAgent = false;
        this.useWebviewAgent = true;
        this.hasImageProcessing = false;
        this.hasBackupURLs = false;
        this.hasCoverBasedPageUpdates = false;
    }

    public static Site searchByCode(long code) {
        for (Site s : values())
            if (s.getCode() == code) return s;

        return NONE;
    }

    // Same as ValueOf with a fallback to NONE
    // (vital for forward compatibility)
    public static Site searchByName(String name) {
        for (Site s : values())
            if (s.name().equalsIgnoreCase(name)) return s;

        return NONE;
    }

    @Nullable
    public static Site searchByUrl(String url) {
        if (null == url || url.isEmpty()) {
            Timber.w("Invalid url");
            return null;
        }

        for (Site s : Site.values())
            if (s.code > 0 && HttpHelper.getDomainFromUri(url).equalsIgnoreCase(HttpHelper.getDomainFromUri(s.url)))
                return s;

        return Site.NONE;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public int getIco() {
        return ico;
    }

    public boolean useMobileAgent() {
        return useMobileAgent;
    }

    public boolean useHentoidAgent() {
        return useHentoidAgent;
    }

    public boolean useWebviewAgent() {
        return useWebviewAgent;
    }

    public boolean hasImageProcessing() {
        return hasImageProcessing;
    }

    public boolean hasBackupURLs() {
        return hasBackupURLs;
    }

    public boolean hasCoverBasedPageUpdates() {
        return hasCoverBasedPageUpdates;
    }

    public boolean isVisible() {
        for (Site s : INVISIBLE_SITES) if (s.equals(this)) return false;
        return true;
    }

    public String getFolder() {
        if (this == FAKKU)
            return "Downloads";
        else
            return description;
    }

    public String getUserAgent() {
        if (useMobileAgent())
            return HttpHelper.getMobileUserAgent(useHentoidAgent(), useWebviewAgent());
        else
            return HttpHelper.getDesktopUserAgent(useHentoidAgent(), useWebviewAgent());
    }

    public static class SiteConverter implements PropertyConverter<Site, Long> {
        @Override
        public Site convertToEntityProperty(Long databaseValue) {
            if (databaseValue == null) {
                return Site.NONE;
            }
            for (Site site : Site.values()) {
                if (site.getCode() == databaseValue) {
                    return site;
                }
            }
            return Site.NONE;
        }

        @Override
        public Long convertToDatabaseValue(Site entityProperty) {
            return entityProperty == null ? null : (long) entityProperty.getCode();
        }
    }
}
