package com.slotfy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for establishment registration request
 */
public class EstablishmentRegisterRequest {
    
    @NotBlank(message = "Tipo de estabelecimento é obrigatório")
    private String tipoEstabelecimento;
    
    @NotBlank(message = "Nome do estabelecimento é obrigatório")
    @Size(max = 255)
    private String nomeEstabelecimento;
    
    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail deve ter um formato válido")
    @Size(max = 255)
    private String email;
    
    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20)
    private String telefone;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;
    
    // Default constructor
    public EstablishmentRegisterRequest() {}
    
    // Constructor with all fields
    public EstablishmentRegisterRequest(String tipoEstabelecimento, String nomeEstabelecimento, 
                                      String email, String telefone, String senha) {
        this.tipoEstabelecimento = tipoEstabelecimento;
        this.nomeEstabelecimento = nomeEstabelecimento;
        this.email = email;
        this.telefone = telefone;
        this.senha = senha;
    }
    
    // Getters and setters
    public String getTipoEstabelecimento() {
        return tipoEstabelecimento;
    }
    
    public void setTipoEstabelecimento(String tipoEstabelecimento) {
        this.tipoEstabelecimento = tipoEstabelecimento;
    }
    
    public String getNomeEstabelecimento() {
        return nomeEstabelecimento;
    }
    
    public void setNomeEstabelecimento(String nomeEstabelecimento) {
        this.nomeEstabelecimento = nomeEstabelecimento;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefone() {
        return telefone;
    }
    
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    
    public String getSenha() {
        return senha;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }
    
    /**
     * Get category based on establishment type
     */
    public String getCategory() {
        if ("barbearia".equals(tipoEstabelecimento)) {
            return "Barbearia";
        } else if ("salao".equals(tipoEstabelecimento)) {
            return "Salão de Beleza";
        }
        return tipoEstabelecimento;
    }
}