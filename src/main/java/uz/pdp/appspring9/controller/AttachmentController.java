package uz.pdp.appspring9.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uz.pdp.appspring9.entity.Attachment;
import uz.pdp.appspring9.entity.AttachmentContent;
import uz.pdp.appspring9.repository.AttachmentContentRepository;
import uz.pdp.appspring9.repository.AttachmentRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    private final AttachmentContentRepository attachmentContentRepository;
    private final AttachmentRepository attachmentRepository;
    private final String directory = "yuklanganlar";

    @PostMapping("/upload")
    public String uploadFile (MultipartHttpServletRequest request) throws IOException {

        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = request.getFile(fileNames.next());
        if (file != null) {
            String originalFilename = file.getOriginalFilename();
            long size = file.getSize();
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType();
            Attachment attachment = new Attachment();
            attachment.setFileOriginalName(originalFilename);
            attachment.setSize(size);
            attachment.setContentType(contentType);
            Attachment saveAttachment = attachmentRepository.save(attachment);

            AttachmentContent attachmentContent = new AttachmentContent();
            attachmentContent.setContentBytes(bytes);
            attachmentContent.setAttachment(saveAttachment);
            AttachmentContent save = attachmentContentRepository.save(attachmentContent);
            return "File upload ID + " + saveAttachment.getId();
        }
        return "Xatolik";

    }



    @GetMapping("/getFile/{id}")
    public void getFile (@PathVariable Integer id, HttpServletResponse response) throws IOException {
        Optional<Attachment> byId = attachmentRepository.findById(id);
        if (byId.isPresent()) {
            Attachment attachment = byId.get();
            Optional<AttachmentContent> byAttachmentId = attachmentContentRepository.findByAttachmentId(id);
            if (byAttachmentId.isPresent()) {
                AttachmentContent attachmentContent = byAttachmentId.get();
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + attachment.getFileOriginalName() + "\"");
                response.setContentType(attachment.getContentType());
                FileCopyUtils.copy(attachmentContent.getContentBytes(),response.getOutputStream());
            }
        }
    }

//    @PostMapping("uploadSystem")
//    public String addFile (MultipartHttpServletRequest request) throws IOException {
//        Iterator<String> fileNames = request.getFileNames();
//        MultipartFile file = request.getFile(fileNames.next());
//        if (file != null) {
//            String originalFilename = file.getOriginalFilename();
//            Attachment attachment = new Attachment();
//            attachment.setContentType(file.getContentType());
//            attachment.setFileOriginalName(originalFilename);
//            attachment.setSize(attachment.getSize());
//            String name = UUID.randomUUID().toString();
//
//            String[] split = originalFilename.split("\\.");
//            name = name + "." + split[split.length-1];
//            attachment.setName(name);
//            Attachment saveAttachment = attachmentRepository.save(attachment);
//            Path path = Paths.get(directory+"/"+name);
//            Files.copy(file.getInputStream(),path);
//            return "File saqlandi";
//        }
//        return "Saqlanmadi";
//    }

//    @GetMapping("/getFileSystem/{id}")
//    public void getFileSystem(@PathVariable Integer id, HttpServletResponse response) throws IOException {
//        Optional<Attachment> byId = attachmentRepository.findById(id);
//        if (byId.isPresent()) {
//            Attachment attachment = byId.get();
//            response.setHeader("Content-Disposition",
//                    "attachment; filename=\"" + attachment.getFileOriginalName() + "\"");
//            response.setContentType(attachment.getContentType());
//
//            FileInputStream fileInputStream = new FileInputStream(directory + "/" + attachment.getName());
//            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
//
//        }
//
//    }
}
