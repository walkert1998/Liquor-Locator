package com.liqourlocator.Liquor.Locator.repository;

import com.liqourlocator.Liquor.Locator.model.Establishment;
import org.springframework.data.couchbase.core.query.N1qlPrimaryIndexed;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbasePagingAndSortingRepository;

import java.util.List;

@N1qlPrimaryIndexed
public interface EstablishmentRepository extends CouchbasePagingAndSortingRepository<Establishment, String>
{
    @Query("SELECT b.*, META(b).id as _ID, META(b).cas as _CAS from #{#n1ql.bucket} as b WHERE city_town=$1 AND " +
            "license_type=$2 ORDER BY establishment ASC")
    List<Establishment> findEstablishmentsByTownAndType(String town, String type);

    @Query("SELECT b.*, META(b).id as _ID, META(b).cas as _CAS from #{#n1ql.bucket} as b WHERE city_town=$1 ORDER BY establishment ASC")
    List<Establishment> findEstablishmentsByTown(String town);

}
