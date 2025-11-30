package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.Documento;
import com.autobots.automanager.modelos.AdicionadorLinkDocumento;
import com.autobots.automanager.repositorios.RepositorioDocumento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/documentos")
public class DocumentoControle {
    @Autowired
    private RepositorioDocumento repositorioDocumento;
    @Autowired
    private AdicionadorLinkDocumento adicionadorLinkDocumento;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<Documento>> listarDocumentos() {
        List<Documento> documentos = repositorioDocumento.findAll();
        if (documentos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkDocumento.adicionadorLinkGeral(documentos);
            return new ResponseEntity<>(documentos, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<Documento> findDocById(@PathVariable long id) {
        Optional<Documento> documentoAlvo = repositorioDocumento.findById(id);
        if (documentoAlvo.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Documento documento = documentoAlvo.get();
            adicionadorLinkDocumento.adicionadorLink(documento);
            return new ResponseEntity<>(documento, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<String> cadastrar(@RequestBody Documento documento) {
        repositorioDocumento.save(documento);
        return new ResponseEntity<>("Documento cadastrado com sucesso!", HttpStatus.OK);
    }

    @PutMapping("/atualizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    public ResponseEntity<String> atualizar(@RequestBody Documento documento) {
        if (documento.getId() == null) {
            return new ResponseEntity<>("ID do documento é obrigatório para atualização", HttpStatus.BAD_REQUEST);
        }

        Optional<Documento> documentoBancoOpt = repositorioDocumento.findById(documento.getId());

        if (documentoBancoOpt.isPresent()) {
            Documento documentoBanco = documentoBancoOpt.get();
            documentoBanco.setTipo(documento.getTipo());
            documentoBanco.setDataEmissao(documento.getDataEmissao());
            documentoBanco.setNumero(documento.getNumero());
            repositorioDocumento.save(documentoBanco);
            return new ResponseEntity<>("Documento atualizado com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Documento não encontrado", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/excluir/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> excluir(@PathVariable Long id) {
        if (repositorioDocumento.existsById(id)) {
            repositorioDocumento.deleteById(id);
            return new ResponseEntity<>("Documento excluido com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Documento não encontrado!", HttpStatus.NOT_FOUND);
        }
    }
}