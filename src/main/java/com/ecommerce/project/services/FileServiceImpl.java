package com.ecommerce.project.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile image) throws IOException {
        //file names of current /original file
        String originalFileName=image.getOriginalFilename();
        //generate unique file name
        String randumId= UUID.randomUUID().toString();
        //mat .jpg---->123213.jpg
        String fileName=randumId.concat(originalFileName.substring(originalFileName.lastIndexOf(".")));
        String filePath=path+ File.separator+fileName;
        //check path exists and create
        File folder=new File(path);
        if (!folder.exists())
            folder.mkdir();
        //upload file
        Files.copy(image.getInputStream(), Paths.get(filePath));
        return fileName;

    }
}
