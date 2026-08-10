package com.back.domain.concert.repository

import com.back.domain.concert.entity.Concert
import org.springframework.data.jpa.repository.JpaRepository

interface ConcertRepository : JpaRepository<Concert, Long>
