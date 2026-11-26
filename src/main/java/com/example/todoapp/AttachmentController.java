package com.example.todoapp;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AttachmentController {

    private final TodoService todoService;
    private final TodoAttachmentService attachmentService;
    private final MessageSource messageSource;

    public AttachmentController(TodoService todoService, TodoAttachmentService attachmentService,
            MessageSource messageSource) {
        this.todoService = todoService;
        this.attachmentService = attachmentService;
        this.messageSource = messageSource;
    }

    @PostMapping("/todos/{todoId}/attachments")
    public String upload(@PathVariable Long todoId, @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        if (todoService.findById(todoId) == null) {
            redirectAttributes.addFlashAttribute("message", message("flash.todoNotFound"));
            return "redirect:/todos";
        }
        try {
            attachmentService.add(todoId, file);
            redirectAttributes.addFlashAttribute("message", message("flash.fileAdded"));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("message", exception.getMessage());
        }
        return "redirect:/todos/" + todoId + "/edit";
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        TodoAttachment attachment = attachmentService.findById(id);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }
        Path path = attachmentService.loadPath(id);
        try {
            MediaType mediaType = attachment.getContentType() == null
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(attachment.getContentType());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(attachment.getOriginalFilename())
                    .build());
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(Files.size(path))
                    .headers(headers)
                    .body(new InputStreamResource(Files.newInputStream(path)));
        } catch (Exception exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/attachments/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        TodoAttachment attachment = attachmentService.findById(id);
        if (attachment == null) {
            redirectAttributes.addFlashAttribute("message", message("flash.fileNotFound"));
            return "redirect:/todos";
        }
        attachmentService.delete(id);
        redirectAttributes.addFlashAttribute("message", message("flash.fileDeleted"));
        return "redirect:/todos/" + attachment.getTodoId() + "/edit";
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
