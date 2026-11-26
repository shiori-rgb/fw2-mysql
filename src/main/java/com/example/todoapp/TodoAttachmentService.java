package com.example.todoapp;

import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TodoAttachmentService {

    private final TodoAttachmentMapper attachmentMapper;
    private final FileStorageService fileStorageService;

    public TodoAttachmentService(TodoAttachmentMapper attachmentMapper,
            FileStorageService fileStorageService) {
        this.attachmentMapper = attachmentMapper;
        this.fileStorageService = fileStorageService;
    }

    public List<TodoAttachment> findByTodoId(Long todoId) {
        return attachmentMapper.findByTodoId(todoId);
    }

    public TodoAttachment findById(Long id) {
        return attachmentMapper.findById(id);
    }

    public TodoAttachment add(Long todoId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ファイルを選択してください");
        }
        FileStorageService.StoredFile stored = fileStorageService.store(file);
        try {
            TodoAttachment attachment = new TodoAttachment();
            attachment.setTodoId(todoId);
            attachment.setOriginalFilename(stored.originalFilename());
            attachment.setStoredFilename(stored.storedFilename());
            attachment.setContentType(stored.contentType());
            attachment.setFileSize(stored.size());
            attachmentMapper.insert(attachment);
            return attachment;
        } catch (RuntimeException exception) {
            fileStorageService.delete(stored.storedFilename());
            throw exception;
        }
    }

    public void delete(Long id) {
        TodoAttachment attachment = requireAttachment(id);
        attachmentMapper.deleteById(id);
        fileStorageService.delete(attachment.getStoredFilename());
    }

    public Path loadPath(Long id) {
        TodoAttachment attachment = requireAttachment(id);
        return fileStorageService.load(attachment.getStoredFilename());
    }

    private TodoAttachment requireAttachment(Long id) {
        TodoAttachment attachment = attachmentMapper.findById(id);
        if (attachment == null) {
            throw new IllegalArgumentException("添付ファイルが見つかりません: " + id);
        }
        return attachment;
    }
}
