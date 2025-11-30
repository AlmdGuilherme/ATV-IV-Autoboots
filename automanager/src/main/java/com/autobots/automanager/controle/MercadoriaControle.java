package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.Mercadoria;
import com.autobots.automanager.modelos.AdicionadorLinkMercadoria;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/mercadorias")
public class MercadoriaControle {
    @Autowired
    private RepositorioMercadoria repositorioMercadoria;
    @Autowired
    private AdicionadorLinkMercadoria adicionadorLinkMercadoria;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<List<Mercadoria>> listarMercadoria() {
        List<Mercadoria> mercadorias = repositorioMercadoria.findAll();
        if (mercadorias.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            adicionadorLinkMercadoria.adicionadorLinkGeral(mercadorias);
            return new ResponseEntity<>(mercadorias, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Mercadoria> buscarMercadoriaPorId(@PathVariable long id) {
        Optional<Mercadoria> mercadoriaOpt = repositorioMercadoria.findById(id);
        if (mercadoriaOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Mercadoria mercadoria = mercadoriaOpt.get();
            adicionadorLinkMercadoria.adicionadorLink(mercadoria);
            return new ResponseEntity<>(mercadoria, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> cadastrarMercadoria(@RequestBody Mercadoria mercadoria) {
        mercadoria.setCadastro(new Date());
        repositorioMercadoria.save(mercadoria);
        return new ResponseEntity<>("Mercadoria cadastrada com sucesso", HttpStatus.CREATED);
    }

    @PutMapping("/atualizar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> atualizarMercadoria(@PathVariable long id, @RequestBody Mercadoria mercadoria) {
        if (mercadoria == null) {
            return new ResponseEntity<>("Não é possível passar valores nulos para atualizar uma mercadoria!", HttpStatus.BAD_REQUEST);
        }

        Optional<Mercadoria> mercadoriaBancoOpt = repositorioMercadoria.findById(id);

        if (mercadoriaBancoOpt.isPresent()) {
            Mercadoria mercadoriaBanco = mercadoriaBancoOpt.get();
            mercadoriaBanco.setValidade(mercadoria.getValidade());
            mercadoriaBanco.setFabricao(mercadoria.getFabricao());
            mercadoriaBanco.setNome(mercadoria.getNome());
            mercadoriaBanco.setQuantidade(mercadoria.getQuantidade());
            mercadoriaBanco.setValor(mercadoria.getValor());
            mercadoriaBanco.setDescricao(mercadoria.getDescricao());

            repositorioMercadoria.save(mercadoriaBanco);
            return new ResponseEntity<>("Mercadoria atualizada com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Não foi possível encontrar a mercadoria!", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/excluir/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> excluirMercadoria(@PathVariable long id) {
        if (repositorioMercadoria.existsById(id)) {
            repositorioMercadoria.deleteById(id);
            return new ResponseEntity<>("Mercadoria excluida com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Mercadoria não encontrada!", HttpStatus.NOT_FOUND);
        }
    }
}