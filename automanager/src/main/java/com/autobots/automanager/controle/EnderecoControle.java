package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.Endereco;
import com.autobots.automanager.modelos.AdicionadorLinkEnderecos;
import com.autobots.automanager.repositorios.RepositorioEndereco;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/enderecos")
public class EnderecoControle {
    @Autowired
    private RepositorioEndereco repositorioEndereco;
    @Autowired
    private AdicionadorLinkEnderecos adicionadorLinkEnderecos;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<Endereco>> listarEnderecos() {
        List<Endereco> enderecos = repositorioEndereco.findAll();
        if (enderecos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkEnderecos.adicionadorLinkGeral(enderecos);
            return new ResponseEntity<>(enderecos, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Endereco> findAddressById(@PathVariable long id) {
        Optional<Endereco> enderecoOpt = repositorioEndereco.findById(id);
        if (enderecoOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Endereco endereco = enderecoOpt.get();
            adicionadorLinkEnderecos.adicionadorLink(endereco);
            return new ResponseEntity<>(endereco, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Endereco> cadastrarEndereco(@RequestBody Endereco endereco) {
        repositorioEndereco.save(endereco);
        return new ResponseEntity<>(endereco, HttpStatus.CREATED);
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<String> atualizarEndereco(@PathVariable long id, @RequestBody Endereco enderecoAtualizar) {
        if (enderecoAtualizar == null) {
            return new ResponseEntity<>("Dados do endereço são obrigatórios", HttpStatus.BAD_REQUEST);
        }

        Optional<Endereco> enderecoBancoOpt = repositorioEndereco.findById(id);

        if (enderecoBancoOpt.isPresent()) {
            Endereco enderecoBanco = enderecoBancoOpt.get();
            enderecoBanco.setBairro(enderecoAtualizar.getBairro());
            enderecoBanco.setRua(enderecoAtualizar.getRua());
            enderecoBanco.setCidade(enderecoAtualizar.getCidade());
            enderecoBanco.setEstado(enderecoAtualizar.getEstado());
            enderecoBanco.setNumero(enderecoAtualizar.getNumero());
            enderecoBanco.setCodigoPostal(enderecoAtualizar.getCodigoPostal());
            enderecoBanco.setInformacoesAdicionais(enderecoAtualizar.getInformacoesAdicionais());

            repositorioEndereco.save(enderecoBanco);
            return new ResponseEntity<>("Endereço atualizado com sucesso", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Não foi possível encontrar este endereço", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/excluir/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> excluirEndereco(@PathVariable long id) {
        if (repositorioEndereco.existsById(id)) {
            repositorioEndereco.deleteById(id);
            return new ResponseEntity<>("Endereço excluido com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Não foi possível encontrar este endereço!", HttpStatus.NOT_FOUND);
        }
    }
}