package com.rest.app;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    private final EmployeeModelAssembler assembler;

    EmployeeController(EmployeeRepository repository, EmployeeModelAssembler assembler) {
        employeeRepository = repository;
        this.assembler = assembler;
    }

    @GetMapping("/employees")
    CollectionModel<EntityModel<Employee>> all() {
       /* return employeeRepository.findAll();*/
        /*List<EntityModel<Employee>> employees =
                employeeRepository.findAll().stream()
                        .map(employee -> EntityModel.of(employee,
                                linkTo(methodOn(EmployeeController.class).one(employee.getId())).withSelfRel(),
                                linkTo(methodOn(EmployeeController.class).all()).withRel("employees")))
                        .collect(Collectors.toList());
        return CollectionModel.of(employees,
                linkTo(methodOn(EmployeeController.class).all()).withSelfRel());*/
        List<EntityModel<Employee>> employees =
                employeeRepository.findAll().stream().map(assembler::toModel).collect(Collectors.toList());
        return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
    }

    @PostMapping("/employees")
    ResponseEntity<?> newEmployee(
            @RequestBody Employee employee
    ) {
        /*return employeeRepository.save(employee);*/
        EntityModel<Employee> entityModel = assembler.toModel(employeeRepository.save(employee));

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/employees/{id}")
    EntityModel<Employee> one(
            @PathVariable("id") Long id
    ) {
        /*
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
                */

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(
                        () -> new EmployeeNotFoundException(id)
                );
/*        return EntityModel.of(employee,
                linkTo(methodOn(EmployeeController.class).one(id)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees")
        );*/
        return assembler.toModel(employee);
    }

    @PutMapping("/employees/{id}")
    Employee replaceEmployee(
            @RequestBody Employee newEmployee,
            @PathVariable Long id
    ) {
        return employeeRepository.findById(id)
                .map(employee -> {
                   employee.setName(newEmployee.getName());
                   employee.setRole(newEmployee.getRole());
                   return employeeRepository.save(employee);
                }).orElseGet(() -> {
                    newEmployee.setId(id);
                    return employeeRepository.save(newEmployee);
                });
    }

    @DeleteMapping("/employees/{id}")
    void deleteEmployee(
            @PathVariable Long id
    ) {
        employeeRepository.deleteById(id);
    }


}
