package com.autobots.automanager.controle;

import com.autobots.automanager.entitades.*;
import com.autobots.automanager.modelos.AdicionadorLinkEmpresa;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/empresas")
public class EmpresaControle {
    @Autowired
    private RepositorioEmpresa repositorioEmpresa;
    @Autowired
    private AdicionadorLinkEmpresa adicionadorLinkEmpresa;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<List<Empresa>> listarEmpresas() {
        List<Empresa> empresas = repositorioEmpresa.findAll();
        if (empresas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLinkEmpresa.adicionadorLinkGeral(empresas);
            return new ResponseEntity<>(empresas, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Empresa> findCompanyById(@PathVariable Long id) {
        Optional<Empresa> empresaOpt = repositorioEmpresa.findById(id);
        if (empresaOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            Empresa empresa = empresaOpt.get();
            adicionadorLinkEmpresa.adicionadorLink(empresa);
            return new ResponseEntity<>(empresa, HttpStatus.OK);
        }
    }

    @PostMapping("/cadastrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cadastrarEmpresa(@RequestBody Empresa empresa) {
        empresa.setCadastro(new Date());
        repositorioEmpresa.save(empresa);
        return new ResponseEntity<>("Empresa cadastrada com sucesso!", HttpStatus.OK);
    }

    @PutMapping("/adicionarTelefones/{companyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adicionarTelefone(@PathVariable long companyId, @RequestBody List<Telefone> telefones) {
        Optional<Empresa> empresaOpt = repositorioEmpresa.findById(companyId);
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            empresa.getTelefones().addAll(telefones);
            repositorioEmpresa.save(empresa);
            return new ResponseEntity<>("Telefones adicionados com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Empresa não encontrada", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/adicionarUsuarios/{companyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adicionarUsuarios(@PathVariable long companyId, @RequestBody List<Usuario> usuarios) {
        Optional<Empresa> empresaOpt = repositorioEmpresa.findById(companyId);
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            empresa.getUsuarios().addAll(usuarios);
            repositorioEmpresa.save(empresa);
            return new ResponseEntity<>("Usuários adicionados com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Empresa não encontrada", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/adicionarServicos/{companyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> adicionarServicos(@PathVariable long companyId, @RequestBody List<Servico> servicos) {
        Optional<Empresa> empresaOpt = repositorioEmpresa.findById(companyId);
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            empresa.getServicos().addAll(servicos);
            repositorioEmpresa.save(empresa);
            return new ResponseEntity<>("Serviços adicionados com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Empresa não encontrada", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/adicionarMercadorias/{companyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<String> adicionarMercadorias(@PathVariable long companyId, @RequestBody List<Mercadoria> mercadorias) {
        Optional<Empresa> empresaOpt = repositorioEmpresa.findById(companyId);
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            empresa.getMercadorias().addAll(mercadorias);
            repositorioEmpresa.save(empresa);
            return new ResponseEntity<>("Mercadorias adicionadas com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Empresa não encontrada", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> atualizarEmpresa(@RequestBody Empresa empresa) {
        if (empresa.getId() == null) {
            return new ResponseEntity<>("ID da empresa é obrigatório", HttpStatus.BAD_REQUEST);
        }

        Optional<Empresa> empresaBancoOpt = repositorioEmpresa.findById(empresa.getId());

        if (empresaBancoOpt.isPresent()) {
            Empresa empresaBanco = empresaBancoOpt.get();
            empresaBanco.setRazaoSocial(empresa.getRazaoSocial());
            empresaBanco.setNomeFantasia(empresa.getNomeFantasia());
            if (empresa.getEndereco() != null) empresaBanco.setEndereco(empresa.getEndereco());

            repositorioEmpresa.save(empresaBanco);
            return new ResponseEntity<>("Empresa atualizada com sucesso", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Empresa não encontrada!", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/excluir/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> excluirEmpresa(@PathVariable Long id) {
        if (repositorioEmpresa.existsById(id)) {
            repositorioEmpresa.deleteById(id);
            return new ResponseEntity<>("Empresa excluida com sucesso!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Empresa não encontrada!", HttpStatus.NOT_FOUND);
        }
    }
}