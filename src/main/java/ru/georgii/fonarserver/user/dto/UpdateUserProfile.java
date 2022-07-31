package ru.georgii.fonarserver.user.dto;

import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

public class UpdateUserProfile {

    @NotNull
    @Length(min = 1, max = 30)
    public String firstname;

    @Length(min = 0, max = 30)
    public String lastname;

    @Length(min = 0, max = 255)
    public String bio;

    @NotNull
    @Length(min = 1, max = 30)
    @Column(unique = true)
    public String nickname;

}
