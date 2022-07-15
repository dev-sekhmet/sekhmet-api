package com.sekhmet.sekhmetapi.service.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConversationDto {

    private List<UUID> ids;
    private String friendlyName;
    private String description;
}
