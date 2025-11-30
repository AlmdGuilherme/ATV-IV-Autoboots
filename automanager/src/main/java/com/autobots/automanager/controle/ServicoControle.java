package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.Servico;
import com.autobots.automanager.modelos.AdicionadorLinkServico;
import com.autobots.automanager.repositorios.RepositorioServico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/servicos")
public class ServicoControle {
    @Autowired
    private RepositorioServico repositorioServico;
    @Autowired
    private AdicionadorLinkServico adicionadorLinkServico;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<List<Servico>> listarServicos() {
        List<Servico> servicos = repositorioServico.findAll();
        if (servicos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkServico.adicionadorLinkGeral(servicos);
            return new ResponseEntity<>(servicos, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Servico> buscarServicoPorId(@PathVariable Long id) {
        Optional<Servico> servicoOpt = repositorioServico.findById(id);
        if (servicoOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Servico servico = servicoOpt.get();
            adicionadorLinkServico.adicionadorLink(servico);
            return new ResponseEntity<>(servico, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> cadastrarServico(@RequestBody Servico servico) {
        if (servico == null) {
            return new ResponseEntity<>("É preciso ter um serviço para cadastrar!", HttpStatus.BAD_REQUEST);
        } else {
            repositorioServico.save(servico);
            return new ResponseEntity<>("Serviço cadastrado com sucesso!", HttpStatus.CREATED);
        }
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> atualizarServico(@PathVariable Long id, @RequestBody Servico servico) {
        Optional<Servico> servicoBancoOpt = repositorioServico.findById(id);

        if (servicoBancoOpt.isPresent()) {
            Servico servicoBanco = servicoBancoOpt.get();
            servicoBanco.setNome(servico.getNome());
            servicoBanco.setValor(servico.getValor());
            servicoBanco.setDescricao(servico.getDescricao());
            repositorioServico.save(servicoBanco);
            return new ResponseEntity<>("Serviço atualizado com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Não foi possível encontrar este serviço...", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/excluir/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> excluirServico(@PathVariable Long id) {
        if (repositorioServico.existsById(id)) {
            repositorioServico.deleteById(id);
            return new ResponseEntity<>("Serviço deletado com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Não foi possível encontrar este serviço!", HttpStatus.NOT_FOUND);
        }
    }
}