package com.utp.biblioteca.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class ArchivoStorageService {

    @Value("${biblioteca.upload.dir:uploads/pdfs}")
    private String uploadDir;

    /**
     * Guarda un archivo PDF en el almacenamiento del servidor.
     * Retorna la ruta relativa para persistirla en la BD.
     */
    public String guardarPdf(MultipartFile file, String prefijo) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo PDF está vacío.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Solo se permiten archivos en formato PDF (.pdf).");
        }

        // Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Nombre de archivo seguro y único
        String nombreLimpio = originalName.replaceAll("[^a-zA-Z0-9.-]", "_");
        String nombreFinal = (prefijo != null ? prefijo + "_" : "") + System.currentTimeMillis() + "_" + nombreLimpio;

        Path destino = uploadPath.resolve(nombreFinal);
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        log.info("PDF guardado exitosamente en: {}", destino.toAbsolutePath());
        return "/" + uploadDir.replace("\\", "/") + "/" + nombreFinal;
    }

    /**
     * Elimina un archivo físico del disco si existe.
     */
    public void eliminarArchivo(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return;
        }
        try {
            String rutaLimpia = rutaRelativa.startsWith("/") ? rutaRelativa.substring(1) : rutaRelativa;
            Path path = Paths.get(rutaLimpia);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Archivo eliminado del servidor: {}", path.toAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("No se pudo eliminar el archivo físico {}: {}", rutaRelativa, e.getMessage());
        }
    }
}
