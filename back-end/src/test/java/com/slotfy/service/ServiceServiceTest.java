package com.slotfy.service;

import com.slotfy.model.Service;
import com.slotfy.model.ServiceStatus;
import com.slotfy.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ServiceService
 */
public class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    private ServiceService serviceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serviceService = new ServiceService(serviceRepository);
    }

    @Test
    void testServiceCreation() {
        assertNotNull(serviceService);
    }

    @Test
    void testGetByEstablishmentId() {
        Long establishmentId = 1L;
        List<Service> expectedServices = Arrays.asList(
            new Service("Service 1", "Description 1", 60, new BigDecimal("50.00"), establishmentId),
            new Service("Service 2", "Description 2", 90, new BigDecimal("75.00"), establishmentId)
        );

        when(serviceRepository.findByEstablishmentIdOrderByNameAsc(establishmentId))
            .thenReturn(expectedServices);

        List<Service> result = serviceService.getByEstablishmentId(establishmentId);

        assertEquals(expectedServices, result);
        verify(serviceRepository).findByEstablishmentIdOrderByNameAsc(establishmentId);
    }

    @Test
    void testGetActiveByEstablishmentId() {
        Long establishmentId = 1L;
        List<Service> activeServices = Arrays.asList(
            new Service("Active Service", "Description", 60, new BigDecimal("50.00"), establishmentId)
        );

        when(serviceRepository.findByEstablishmentIdAndStatusOrderByNameAsc(establishmentId, ServiceStatus.ACTIVE))
            .thenReturn(activeServices);

        List<Service> result = serviceService.getActiveByEstablishmentId(establishmentId);

        assertEquals(activeServices, result);
        verify(serviceRepository).findByEstablishmentIdAndStatusOrderByNameAsc(establishmentId, ServiceStatus.ACTIVE);
    }

    @Test
    void testGetByCategory() {
        Long establishmentId = 1L;
        String category = "Haircut";
        List<Service> categoryServices = Arrays.asList(
            new Service("Haircut Service", "Description", 45, new BigDecimal("40.00"), establishmentId)
        );

        when(serviceRepository.findByEstablishmentIdAndCategoryOrderByNameAsc(establishmentId, category))
            .thenReturn(categoryServices);

        List<Service> result = serviceService.getByCategory(establishmentId, category);

        assertEquals(categoryServices, result);
        verify(serviceRepository).findByEstablishmentIdAndCategoryOrderByNameAsc(establishmentId, category);
    }

    @Test
    void testCreateService_Success() {
        String name = "New Service";
        String description = "Service Description";
        Integer durationMinutes = 60;
        BigDecimal price = new BigDecimal("50.00");
        Long establishmentId = 1L;
        String category = "Category";

        when(serviceRepository.findByNameAndEstablishmentId(name, establishmentId))
            .thenReturn(Optional.empty());

        Service savedService = new Service(name, description, durationMinutes, price, establishmentId);
        savedService.setId(1L);
        when(serviceRepository.save(any(Service.class))).thenReturn(savedService);

        Service result = serviceService.createService(name, description, durationMinutes, price, establishmentId, category);

        assertNotNull(result);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    void testCreateService_NullName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.createService(null, "Description", 60, new BigDecimal("50.00"), 1L, "Category");
        });
        
        assertEquals("Nome do serviço é obrigatório", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testCreateService_EmptyName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.createService("   ", "Description", 60, new BigDecimal("50.00"), 1L, "Category");
        });
        
        assertEquals("Nome do serviço é obrigatório", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testCreateService_InvalidDuration() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.createService("Service", "Description", 0, new BigDecimal("50.00"), 1L, "Category");
        });
        
        assertEquals("Duração deve ser maior que zero", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testCreateService_NullDuration() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.createService("Service", "Description", null, new BigDecimal("50.00"), 1L, "Category");
        });
        
        assertEquals("Duração deve ser maior que zero", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testCreateService_NegativePrice() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.createService("Service", "Description", 60, new BigDecimal("-10.00"), 1L, "Category");
        });
        
        assertEquals("Preço não pode ser negativo", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testCreateService_NullPrice() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.createService("Service", "Description", 60, null, 1L, "Category");
        });
        
        assertEquals("Preço não pode ser negativo", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testCreateService_NullEstablishmentId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.createService("Service", "Description", 60, new BigDecimal("50.00"), null, "Category");
        });
        
        assertEquals("ID do estabelecimento é obrigatório", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testCreateService_NameAlreadyExists() {
        String name = "Existing Service";
        Long establishmentId = 1L;

        Service existing = new Service(name, "Description", 60, new BigDecimal("50.00"), establishmentId);
        when(serviceRepository.findByNameAndEstablishmentId(name, establishmentId))
            .thenReturn(Optional.of(existing));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.createService(name, "New Description", 90, new BigDecimal("75.00"), establishmentId, "Category");
        });
        
        assertEquals("Já existe um serviço com este nome neste estabelecimento", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testUpdateService_Success() {
        Long serviceId = 1L;
        Long establishmentId = 1L;
        String newName = "Updated Service";
        String newDescription = "Updated Description";
        Integer newDuration = 90;
        BigDecimal newPrice = new BigDecimal("75.00");
        String newCategory = "New Category";

        Service service = new Service("Old Service", "Old Description", 60, new BigDecimal("50.00"), establishmentId);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.existsByNameAndEstablishmentIdAndIdNot(newName, establishmentId, serviceId))
            .thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        Service result = serviceService.updateService(serviceId, newName, newDescription, newDuration, newPrice, newCategory);

        assertNotNull(result);
        verify(serviceRepository).save(service);
    }

    @Test
    void testUpdateService_NotFound() {
        Long serviceId = 1L;

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.updateService(serviceId, "Name", "Description", 60, new BigDecimal("50.00"), "Category");
        });
        
        assertEquals("Serviço não encontrado", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testUpdateService_NullName() {
        Long serviceId = 1L;
        Service service = new Service("Old Service", "Description", 60, new BigDecimal("50.00"), 1L);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.updateService(serviceId, null, "Description", 60, new BigDecimal("50.00"), "Category");
        });
        
        assertEquals("Nome do serviço é obrigatório", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testUpdateService_NameAlreadyExists() {
        Long serviceId = 1L;
        Long establishmentId = 1L;
        String duplicateName = "Duplicate Service";

        Service service = new Service("Old Service", "Description", 60, new BigDecimal("50.00"), establishmentId);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.existsByNameAndEstablishmentIdAndIdNot(duplicateName, establishmentId, serviceId))
            .thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.updateService(serviceId, duplicateName, "Description", 60, new BigDecimal("50.00"), "Category");
        });
        
        assertEquals("Já existe outro serviço com este nome neste estabelecimento", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testUpdateStatus_Success() {
        Long serviceId = 1L;
        Service service = new Service("Service", "Description", 60, new BigDecimal("50.00"), 1L);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        Service result = serviceService.updateStatus(serviceId, ServiceStatus.INACTIVE);

        assertNotNull(result);
        verify(serviceRepository).save(service);
    }

    @Test
    void testUpdateStatus_NotFound() {
        Long serviceId = 1L;

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.updateStatus(serviceId, ServiceStatus.INACTIVE);
        });
        
        assertEquals("Serviço não encontrado", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testGetCategories() {
        Long establishmentId = 1L;
        List<String> categories = Arrays.asList("Haircut", "Massage", "Spa");

        when(serviceRepository.findDistinctCategoriesByEstablishmentId(establishmentId))
            .thenReturn(categories);

        List<String> result = serviceService.getCategories(establishmentId);

        assertEquals(categories, result);
        verify(serviceRepository).findDistinctCategoriesByEstablishmentId(establishmentId);
    }

    @Test
    void testGetByPriceRange() {
        Long establishmentId = 1L;
        BigDecimal minPrice = new BigDecimal("30.00");
        BigDecimal maxPrice = new BigDecimal("100.00");
        List<Service> priceRangeServices = Arrays.asList(
            new Service("Mid-price Service", "Description", 60, new BigDecimal("50.00"), establishmentId)
        );

        when(serviceRepository.findByEstablishmentIdAndPriceBetween(establishmentId, minPrice, maxPrice))
            .thenReturn(priceRangeServices);

        List<Service> result = serviceService.getByPriceRange(establishmentId, minPrice, maxPrice);

        assertEquals(priceRangeServices, result);
        verify(serviceRepository).findByEstablishmentIdAndPriceBetween(establishmentId, minPrice, maxPrice);
    }

    @Test
    void testGetByDurationRange() {
        Long establishmentId = 1L;
        Integer minDuration = 30;
        Integer maxDuration = 90;
        List<Service> durationRangeServices = Arrays.asList(
            new Service("Mid-duration Service", "Description", 60, new BigDecimal("50.00"), establishmentId)
        );

        when(serviceRepository.findByEstablishmentIdAndDurationBetween(establishmentId, minDuration, maxDuration))
            .thenReturn(durationRangeServices);

        List<Service> result = serviceService.getByDurationRange(establishmentId, minDuration, maxDuration);

        assertEquals(durationRangeServices, result);
        verify(serviceRepository).findByEstablishmentIdAndDurationBetween(establishmentId, minDuration, maxDuration);
    }

    @Test
    void testCountByEstablishment() {
        Long establishmentId = 1L;
        long expectedCount = 10L;

        when(serviceRepository.countByEstablishmentId(establishmentId)).thenReturn(expectedCount);

        long result = serviceService.countByEstablishment(establishmentId);

        assertEquals(expectedCount, result);
        verify(serviceRepository).countByEstablishmentId(establishmentId);
    }

    @Test
    void testCountActiveByEstablishment() {
        Long establishmentId = 1L;
        long expectedCount = 7L;

        when(serviceRepository.countByEstablishmentIdAndStatus(establishmentId, ServiceStatus.ACTIVE))
            .thenReturn(expectedCount);

        long result = serviceService.countActiveByEstablishment(establishmentId);

        assertEquals(expectedCount, result);
        verify(serviceRepository).countByEstablishmentIdAndStatus(establishmentId, ServiceStatus.ACTIVE);
    }

    @Test
    void testDeleteService_Success() {
        Long serviceId = 1L;
        Service service = new Service("Service", "Description", 60, new BigDecimal("50.00"), 1L);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        doNothing().when(serviceRepository).deleteById(serviceId);

        serviceService.deleteService(serviceId);

        verify(serviceRepository).deleteById(serviceId);
    }

    @Test
    void testDeleteService_NotFound() {
        Long serviceId = 1L;

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.deleteService(serviceId);
        });
        
        assertEquals("Serviço não encontrado", exception.getMessage());
        verify(serviceRepository, never()).deleteById(anyLong());
    }

    @Test
    void testUpdateImage_Success() {
        Long serviceId = 1L;
        Long establishmentId = 1L;
        String imageUrl = "https://example.com/image.jpg";

        Service service = new Service("Service", "Description", 60, new BigDecimal("50.00"), establishmentId);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        Service result = serviceService.updateImage(serviceId, imageUrl, establishmentId);

        assertNotNull(result);
        assertEquals(imageUrl, result.getImageUrl());
        verify(serviceRepository).save(service);
    }

    @Test
    void testUpdateImage_ServiceNotFound() {
        Long serviceId = 1L;
        Long establishmentId = 1L;
        String imageUrl = "https://example.com/image.jpg";

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serviceService.updateImage(serviceId, imageUrl, establishmentId);
        });
        
        assertEquals("Serviço não encontrado", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testUpdateImage_WrongEstablishment() {
        Long serviceId = 1L;
        Long establishmentId = 1L;
        Long wrongEstablishmentId = 2L;
        String imageUrl = "https://example.com/image.jpg";

        Service service = new Service("Service", "Description", 60, new BigDecimal("50.00"), establishmentId);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        Exception exception = assertThrows(SecurityException.class, () -> {
            serviceService.updateImage(serviceId, imageUrl, wrongEstablishmentId);
        });
        
        assertEquals("Acesso negado: serviço não pertence ao estabelecimento", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testUpdateServiceWithEstablishment_Success() {
        Long serviceId = 1L;
        Long establishmentId = 1L;
        String newName = "Updated Service";
        String newDescription = "Updated Description";
        Integer newDuration = 90;
        BigDecimal newPrice = new BigDecimal("75.00");
        String newCategory = "New Category";

        Service service = new Service("Old Service", "Old Description", 60, new BigDecimal("50.00"), establishmentId);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(serviceRepository.existsByNameAndEstablishmentIdAndIdNot(newName, establishmentId, serviceId))
            .thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        Service result = serviceService.updateService(serviceId, newName, newDescription, newDuration, newPrice, newCategory, establishmentId);

        assertNotNull(result);
        verify(serviceRepository).save(service);
    }

    @Test
    void testUpdateServiceWithEstablishment_WrongEstablishment() {
        Long serviceId = 1L;
        Long establishmentId = 1L;
        Long wrongEstablishmentId = 2L;

        Service service = new Service("Service", "Description", 60, new BigDecimal("50.00"), establishmentId);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        Exception exception = assertThrows(SecurityException.class, () -> {
            serviceService.updateService(serviceId, "Name", "Description", 60, new BigDecimal("50.00"), "Category", wrongEstablishmentId);
        });
        
        assertEquals("Acesso negado: serviço não pertence ao estabelecimento", exception.getMessage());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    void testDeleteServiceWithEstablishment_Success() {
        Long serviceId = 1L;
        Long establishmentId = 1L;

        Service service = new Service("Service", "Description", 60, new BigDecimal("50.00"), establishmentId);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        doNothing().when(serviceRepository).deleteById(serviceId);

        serviceService.deleteService(serviceId, establishmentId);

        verify(serviceRepository).deleteById(serviceId);
    }

    @Test
    void testDeleteServiceWithEstablishment_WrongEstablishment() {
        Long serviceId = 1L;
        Long establishmentId = 1L;
        Long wrongEstablishmentId = 2L;

        Service service = new Service("Service", "Description", 60, new BigDecimal("50.00"), establishmentId);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        Exception exception = assertThrows(SecurityException.class, () -> {
            serviceService.deleteService(serviceId, wrongEstablishmentId);
        });
        
        assertEquals("Acesso negado: serviço não pertence ao estabelecimento", exception.getMessage());
        verify(serviceRepository, never()).deleteById(anyLong());
    }

    @Test
    void testFindByIdAndEstablishment_Success() {
        Long serviceId = 1L;
        Long establishmentId = 1L;

        Service service = new Service("Service", "Description", 60, new BigDecimal("50.00"), establishmentId);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        Optional<Service> result = serviceService.findByIdAndEstablishment(serviceId, establishmentId);

        assertTrue(result.isPresent());
        assertEquals(service, result.get());
    }

    @Test
    void testFindByIdAndEstablishment_WrongEstablishment() {
        Long serviceId = 1L;
        Long establishmentId = 1L;
        Long wrongEstablishmentId = 2L;

        Service service = new Service("Service", "Description", 60, new BigDecimal("50.00"), establishmentId);
        service.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        Optional<Service> result = serviceService.findByIdAndEstablishment(serviceId, wrongEstablishmentId);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByIdAndEstablishment_NotFound() {
        Long serviceId = 1L;
        Long establishmentId = 1L;

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        Optional<Service> result = serviceService.findByIdAndEstablishment(serviceId, establishmentId);

        assertFalse(result.isPresent());
    }
}
