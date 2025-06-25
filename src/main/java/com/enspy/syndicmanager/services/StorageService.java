package com.enspy.syndicmanager.services;

import com.enspy.syndicmanager.dto.response.ResponseDto;
import com.enspy.syndicmanager.models.Branch;
import com.enspy.syndicmanager.models.OrganisationUnion;
import com.enspy.syndicmanager.models.SyndUser;
import com.enspy.syndicmanager.repositories.BranchRepository;
import com.enspy.syndicmanager.repositories.OrganisationUnionRepositories;
import com.enspy.syndicmanager.repositories.SyndUserRepository;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class StorageService {

    @Autowired
    ServletContext context;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    SyndUserRepository syndUserRepository;

    @Autowired
    OrganisationUnionRepositories organisationUnionRepository;

    @Value("${syndicmanager.file-storage}")
    private String filepath;

    /**
     * Génère un nom aléatoire de la longueur spécifiée.
     * Le nom est composé de chiffres et de lettres (majuscule et minuscule).
     *
     * @param length La longueur du nom à générer.
     * @return Un Mono contenant une chaîne de caractères représentant le nom généré.
     */
    public Mono<String> nameGenerator(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return Mono.just(generatedString);
    }

    /**
     * Obtient le chemin du répertoire où les fichiers seront stockés.
     * Si le répertoire n'existe pas, il est créé.
     *
     * @return Un Mono contenant le chemin du répertoire de stockage.
     */
    public Mono<String> getUploadPath() {
        String uploadDir = System.getProperty("user.dir") + "/" + filepath;
        if (!new File(uploadDir).exists()) {
            new File(uploadDir).mkdir();
        }
        return Mono.just(uploadDir);
    }

    /**
     * Récupère le chemin ou les images d'une Branch seront stockés
     * si le chemin n'éxite pas il est crée
     * @param branchId
     * @return Un Mono contenant le repertoire de stockage des média pour une branche donnée
     */
    public Mono<String> getBranchPath(UUID branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("the Branch not exist"));
        String mediaFolder = branch.getMediaFolder();
        String uploadDir = System.getProperty("user.dir") + "/" + filepath;
        if (!new File(uploadDir).exists()) {
            new File(uploadDir).mkdir();
        }

        if(mediaFolder == null || mediaFolder.length() == 0){
            mediaFolder = nameGenerator(10).block(); // Synchronous call for simplicity
            branch.setMediaFolder(mediaFolder);
            branchRepository.save(branch);
            new File(uploadDir + "/" + mediaFolder).mkdir();
        } else {
            new File(uploadDir + "/" + mediaFolder).mkdir();
        }
        return Mono.just(uploadDir + "/" + mediaFolder);
    }



    public Mono<String> getUnionPath(UUID unionId){
        OrganisationUnion union = organisationUnionRepository.findById(unionId)
          .orElseThrow(() -> new RuntimeException("Union not exist"));
        String mediaFolder = union.getMediaFolder();
        String uploadDir = System.getProperty("user.dir") + "/" + filepath;
        if (!new File(uploadDir).exists()) {
            new File(uploadDir).mkdir();
        }
        if(mediaFolder == null || mediaFolder.length() == 0){
            mediaFolder = nameGenerator(10).block(); // Synchronous call for simplicity
            union.setMediaFolder(mediaFolder);
            organisationUnionRepository.save(union);
            new File(uploadDir + "/" + mediaFolder).mkdir();
        } else {
            new File(uploadDir + "/" + mediaFolder).mkdir();
        }
        return Mono.just(uploadDir + "/" + mediaFolder);
    }

    /**
     * Récupère le repertoire de stockage d'un utilisateur dans une branche donnée
     * Si le repertoire n'éxiste pas il est crée
     * @param userId
     * @param branchId
     * @return Un Mono contenant le reperoire de stockage de l'utilisateur
     */
    public Mono<String> getUserPath(UUID userId, UUID branchId) {
        SyndUser user = syndUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("the user not exist"));

        String folder = user.getFolder();
        String mediaFolder = getBranchPath(branchId).block(); // Synchronous call for simplicity
        String uploadDir = System.getProperty("user.dir") + "/" + filepath;
        if (!new File(uploadDir).exists()) {
            new File(uploadDir).mkdir();
        }

        if (folder == null || folder.length() == 0) {
            folder = nameGenerator(10).block(); // Synchronous call for simplicity
            user.setFolder(folder);
            syndUserRepository.save(user);
            new File(mediaFolder + "/" +  folder).mkdir();
        } else if (!Files.exists(Path.of(mediaFolder + "/" + folder))) {
            new File(mediaFolder + "/" + folder).mkdir();
        }
        return Mono.just(uploadDir + "/" + mediaFolder + "/" + folder);
    }

    /**
     * Enregistre les fichiers KYC de l'utilisateur
     *
     * @param file        Le fichier à enregistrer.
     * @param userId      l'id de l'utilisateur
     * @param branchId    la branche concernée
     *
     */
    public Mono<Void> save(MultipartFile file, UUID userId, UUID branchId) {
        try {
            String userPath = getUserPath(userId, branchId).block(); // Synchronous call for simplicity
            String filename = file.getOriginalFilename();
            String storedFile = "";

            if (!Files.exists(Path.of(userPath + "/KYC"))) {
                new File(userPath + "/KYC").mkdir();
            }
            storedFile = userPath + "/" + "KYC" + "/" + filename;

            File dest = new File(storedFile);
            file.transferTo(dest);
            return Mono.empty();
        } catch (IOException | IllegalStateException e) {
            return Mono.error(new RuntimeException(e));
        }
    }



    public Mono<Void> saveUnionLogo(MultipartFile  file, UUID unionId){
        try {
            String path = getUnionPath(unionId).block(); // Synchronous call for simplicity
            String filename = file.getOriginalFilename();
            String storedFile = "";

            if (!Files.exists(Path.of(path + "/asset"))) {
                new File(path + "/asset").mkdir();
            }
            storedFile = path + "/" + "asset" + "/" + filename;

            File dest = new File(storedFile);
            file.transferTo(dest);
            return Mono.empty();
        } catch (IOException | IllegalStateException e) {
            return Mono.error(new RuntimeException(e));
        }
    }

    /**
     * Charge un fichier à partir de son identifiant unique.
     *
     * @param fileId L'identifiant du fichier.
     * @return Un Mono contenant le fichier sous forme de ressource.
     */
    public Mono<Resource> loadFileAsResource(String fileId) {
        try {
            Path filePath = getFilePathFromId(fileId);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return Mono.just(resource);
            } else {
                return Mono.error(new RuntimeException("File not found: " + fileId));
            }
        } catch (MalformedURLException ex) {
            return Mono.error(new RuntimeException("File not found: " + fileId, ex));
        }
    }

    /**
     * Génère un identifiant unique pour un fichier basé sur l'utilisateur, le type
     * et le nom du fichier.
     *
     * @param username    Le nom d'utilisateur.
     * @param fileName    Le nom du fichier.
     * @param type        Le type de fichier (personId ou phoneNumber).
     * @param phoneNumber Le numéro de téléphone de l'utilisateur (si applicable).
     * @return Un Mono contenant un identifiant unique pour le fichier.
     */
    private Mono<String> generateFileId(String username, String fileName, String type, String phoneNumber) {
        String fileId = null;
        if (type.equals("personId")) {
            fileId = username + "_" + "personId" + "_" + fileName;
        } else if (type.equals("phoneNumber")) {
            fileId = username + "_" + "phoneNumber" + "_" + phoneNumber + "_" + fileName;
        }
        return fileId != null ? Mono.just(fileId) : Mono.empty();
    }

    /**
     * Récupère le chemin d'un fichier à partir de son identifiant unique.
     *
     * @param fileId L'identifiant unique du fichier.
     * @return Le chemin du fichier.
     */
    private Path getFilePathFromId(String fileId) {
        String[] parts = fileId.split("_");
        String username = parts[0];
        String type = parts[1];

        if (type.equals("personId")) {
            String fileName = parts[2];
            return Paths.get(getUserPath(null, null).block() + "/personId", fileName);
        } else if (type.equals("phoneNumber")) {
            String phoneNumber = parts[2];
            String fileName = parts[3];
            return Paths.get(getUserPath(null, null).block() + phoneNumber, fileName);
        }
        return null;
    }

    public Mono<Void> deleteSingleFile(UUID userId, UUID branchId, String fileName) {
        String userPath = this.getUserPath(userId, branchId).block(); // Synchronous call for simplicity

        if (userPath == null || userPath.isEmpty()) {
            return Mono.error(new IllegalArgumentException("User path is invalid or empty"));
        }

        File targetFile = new File(userPath + "/KYC/" + fileName);

        if (targetFile.exists() && !targetFile.isDirectory()) {
            if (targetFile.delete()) {
                System.out.println("File deleted successfully: " + targetFile.getAbsolutePath());
                return Mono.empty();
            } else {
                return Mono.error(new RuntimeException("Failed to delete file: " + targetFile.getAbsolutePath()));
            }
        }
        return Mono.empty();
    }

    public Mono<ResponseDto> UploadMultipleFile(UUID userId, UUID branchId, MultipartFile[] files) {
        Map<String, Object> map = new HashMap<>();
        ResponseDto response = new ResponseDto();

        if (files.length > 0) {
            Arrays.stream(files).forEach((file) -> {
                this.deleteSingleFile(userId, branchId, file.getOriginalFilename()).block();
                this.save(file, userId, branchId).block();
                com.enspy.syndicmanager.models.File uploaded = new com.enspy.syndicmanager.models.File();
                uploaded.setFilename(file.getOriginalFilename());
                uploaded.setContent(file.getContentType());
                uploaded.setSize(file.getSize());
                map.put(file.getOriginalFilename(), uploaded);
            });
        }
        Optional<SyndUser> user = syndUserRepository.findById(userId);
        if (user.isEmpty()) {
            response.setText("Account not found");
            response.setStatus(404);
            response.setData(null);
        } else {
            response.setText("Files Uploaded Successfully");
            response.setStatus(200);
            response.setData(map);
        }

        return Mono.just(response);
    }

    public Mono<List<File>> getAllFilesInDirectory(String directoryName) {
        if (directoryName == null || directoryName.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Le nom du répertoire ne peut pas être null ou vide."));
        }
        File directory = new File(directoryName);
        if (!directory.exists()) {
            return Mono.error(new IllegalArgumentException("Le répertoire spécifié n'existe pas : " + directoryName));
        }
        if (!directory.isDirectory()) {
            return Mono.error(new IllegalArgumentException("Le chemin spécifié n'est pas un répertoire : " + directoryName));
        }
        File[] filesArray = directory.listFiles();
        if (filesArray == null) {
            return Mono.just(new ArrayList<>());
        }
        return Mono.just(Arrays.asList(filesArray));
    }

    public Mono<String> getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return Mono.just(fileName.substring(0, lastDotIndex));
        }
        return Mono.just(fileName);
    }

    public Mono<ResponseEntity<Resource>> getFile(UUID userId, UUID branchId, String fileName) {
        String userPath = this.getUserPath(userId, branchId).block(); // Synchronous call for simplicity

        if (userPath == null || userPath.isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
        }

        File targetFile = new File(userPath + "/KYC/" + fileName);
        String[] possibleExtensions = { ".png", ".PNG", ".jpg", ".jpeg" };

        if (!targetFile.exists() || targetFile.isDirectory()) {
            for (String ext : possibleExtensions) {
                File possibleFile = new File(userPath + "/KYC/" + fileName + ext);
                if (possibleFile.exists() && !possibleFile.isDirectory()) {
                    targetFile = possibleFile;
                    break;
                }
            }
        }

        if (targetFile.exists() && !targetFile.isDirectory()) {
            try {
                String mimeType = Files.probeContentType(targetFile.toPath());
                Resource fileResource = new FileSystemResource(targetFile);

                ResponseEntity<Resource> responseEntity = ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, mimeType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + targetFile.getName() + "\"")
                        .body(fileResource);

                return Mono.just(responseEntity);
            } catch (Exception e) {
                e.printStackTrace();
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
            }
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
        }
    }
}