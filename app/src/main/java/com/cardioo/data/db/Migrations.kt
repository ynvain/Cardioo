package com.cardioo.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    /**
     * v1 -> v2
     * - user: support multiple accounts (auto id + name)
     * - health_measurement: link each row to a userId (FK)
     */
    val MIGRATION_1_2: Migration =
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `height` REAL NOT NULL,
                        `heightUnit` TEXT NOT NULL,
                        `weightUnit` TEXT NOT NULL,
                        `dateOfBirthIso` TEXT,
                        `gender` TEXT
                    )
                    """.trimIndent(),
                )

                // Migrate existing user profile(s) with a generated display name.
                db.execSQL(
                    """
                    INSERT INTO `user_new` (`id`, `name`, `height`, `heightUnit`, `weightUnit`, `dateOfBirthIso`, `gender`)
                    SELECT `id`, 'Account ' || `id`, `height`, `heightUnit`, `weightUnit`, `dateOfBirthIso`, `gender`
                    FROM `user`
                    """.trimIndent(),
                )

                // Ensure at least one account exists for legacy measurement rows.
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `user_new` (`id`, `name`, `height`, `heightUnit`, `weightUnit`, `dateOfBirthIso`, `gender`)
                    VALUES (1, 'Account 1', 170.0, 'CM', 'KG', NULL, NULL)
                    """.trimIndent(),
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `health_measurement_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `timestampEpochMillis` INTEGER NOT NULL,
                        `systolic` INTEGER NOT NULL,
                        `diastolic` INTEGER NOT NULL,
                        `pulse` INTEGER NOT NULL,
                        `weight` REAL NOT NULL,
                        `weightUnit` TEXT NOT NULL,
                        `notes` TEXT,
                        FOREIGN KEY(`userId`) REFERENCES `user_new`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )

                db.execSQL(
                    """
                    INSERT INTO `health_measurement_new` (`id`, `userId`, `timestampEpochMillis`, `systolic`, `diastolic`, `pulse`, `weight`, `weightUnit`, `notes`)
                    SELECT `id`, 1, `timestampEpochMillis`, `systolic`, `diastolic`, `pulse`, `weight`, `weightUnit`, `notes`
                    FROM `health_measurement`
                    """.trimIndent(),
                )

                db.execSQL("DROP TABLE `health_measurement`")
                db.execSQL("ALTER TABLE `health_measurement_new` RENAME TO `health_measurement`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_measurement_timestampEpochMillis` ON `health_measurement` (`timestampEpochMillis`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_measurement_userId` ON `health_measurement` (`userId`)")

                db.execSQL("DROP TABLE `user`")
                db.execSQL("ALTER TABLE `user_new` RENAME TO `user`")
            }
        }

    /**
     * v2 -> v3
     * - pulse and weight on health_measurement become optional (NULL allowed).
     */
    val MIGRATION_2_3: Migration =
        object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `health_measurement_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `timestampEpochMillis` INTEGER NOT NULL,
                        `systolic` INTEGER NOT NULL,
                        `diastolic` INTEGER NOT NULL,
                        `pulse` INTEGER,
                        `weight` REAL,
                        `weightUnit` TEXT NOT NULL,
                        `notes` TEXT,
                        FOREIGN KEY(`userId`) REFERENCES `user`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `health_measurement_new` (`id`, `userId`, `timestampEpochMillis`, `systolic`, `diastolic`, `pulse`, `weight`, `weightUnit`, `notes`)
                    SELECT `id`, `userId`, `timestampEpochMillis`, `systolic`, `diastolic`, `pulse`, `weight`, `weightUnit`, `notes`
                    FROM `health_measurement`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `health_measurement`")
                db.execSQL("ALTER TABLE `health_measurement_new` RENAME TO `health_measurement`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_measurement_timestampEpochMillis` ON `health_measurement` (`timestampEpochMillis`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_measurement_userId` ON `health_measurement` (`userId`)")
            }
        }
}
