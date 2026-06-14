package com.jewelry.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jewelry.entity.Customer;
import com.jewelry.mapper.CustomerMapper;
import com.jewelry.service.CustomerService;
import org.springframework.stereotype.Service;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {
}
