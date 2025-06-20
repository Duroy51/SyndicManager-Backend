package com.enspy.syndicmanager.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class SqliteDirectoryConfig {

    // Extrait la partie "database.db" de jdbc:sqlite:database.db
    @Value("${spring.datasource.url:jdbc:sqlite:database.db}")
    private String datasourceUrl;

    @Bean
    public InitializingBean createSqliteDirectory() {
        return () -> {
            // Retirer le préfixe "jdbc:sqlite:"
            String path = datasourceUrl.replaceFirst("^jdbc:sqlite:", "");
            File dbFile = new File(path);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();  // crée dossier + parents si besoin :contentReference[oaicite:1]{index=1}
                if (!created) {
                    throw new IllegalStateException(
                            "Impossible de créer le répertoire SQLite : " + parentDir.getAbsolutePath()
                    );
                }
            }
        };
    }
}
