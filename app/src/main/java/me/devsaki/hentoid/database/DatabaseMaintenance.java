package me.devsaki.hentoid.database;

import android.content.Context;
import android.util.Pair;

import java.util.List;

import me.devsaki.hentoid.database.domains.Content;
import me.devsaki.hentoid.enums.StatusContent;
import timber.log.Timber;

public class DatabaseMaintenance {

    /**
     * Clean up and upgrade database
     * NB : Heavy operations; must be performed in the background to avoid ANR at startup
     */
    public static void performDatabaseHousekeeping(Context context) {
        HentoidDB oldDb = HentoidDB.getInstance(context);
        ObjectBoxDB db = ObjectBoxDB.getInstance(context);

        Timber.d("Content item(s) count: %s", db.countContentEntries());

        // Set items that were being downloaded in previous session as paused
        Timber.i("Updating queue status : start");
        db.updateContentStatus(StatusContent.DOWNLOADING, StatusContent.PAUSED);
        Timber.i("Updating queue status : done");

        // Clear temporary books created from browsing a book page without downloading it (since versionCode 60 / v1.3.7)
        Timber.i("Clearing temporary books : start");
        List<Content> obsoleteTempContent = db.selectContentByStatus(StatusContent.SAVED);
        Timber.i("Clearing temporary books : %s books detected", obsoleteTempContent.size());
        for (Content c : obsoleteTempContent) db.deleteContent(c);
        Timber.i("Clearing temporary books : done");

        // Perform technical data updates on the old database engine
        if (oldDb.countContentEntries() > 0) {
            oldDb.updateContentStatus(StatusContent.DOWNLOADING, StatusContent.PAUSED);
        }
    }

    /**
     * Handles complex DB version updates at startup
     */
    @SuppressWarnings("deprecation")
    public static void performOldDatabaseUpdate(HentoidDB db) {
        // Update all "storage_folder" fields in CONTENT table (mandatory) (since versionCode 44 / v1.2.2)
        List<Content> contents = db.selectContentEmptyFolder();
        if (contents != null && contents.size() > 0) {
            for (int i = 0; i < contents.size(); i++) {
                Content content = contents.get(i);
                content.setStorageFolder("/" + content.getSite().getDescription() + "/" + content.getOldUniqueSiteId()); // This line must use deprecated code, as it migrates it to newest version
                db.updateContentStorageFolder(content);
            }
        }

        // Migrate the old download queue (books in DOWNLOADING or PAUSED status) in the queue table (since versionCode 60 / v1.3.7)
        // Gets books that should be in the queue but aren't
        List<Integer> contentToMigrate = db.selectContentsForQueueMigration();

        if (contentToMigrate.size() > 0) {
            // Gets last index of the queue
            List<Pair<Integer, Integer>> queue = db.selectQueue();
            int lastIndex = 1;
            if (queue.size() > 0) {
                lastIndex = queue.get(queue.size() - 1).second + 1;
            }

            for (int i : contentToMigrate) {
                db.insertQueue(i, lastIndex++);
            }
        }
    }

    public static boolean hasToMigrate(Context context) {
        HentoidDB oldDb = HentoidDB.getInstance(context);
        return (oldDb.countContentEntries() > 0);
    }
}
