package com.demo.demo.dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String email;
    private String name; // o lo que tengas de usuario público
    private String username;
    private String profileImageUrl;
    private List<Long> favouriteGenresIds;
    private List<Long> favouriteInstrumentsIds;

}