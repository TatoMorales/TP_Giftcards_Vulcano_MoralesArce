package org.udesa.giftcards.model;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public abstract class ModelService <M extends ModelEntity, R extends JpaRepository<M, Long>> {
    @Autowired protected R repository;

    @Transactional(readOnly = true)
    public List<M> findAll() {
        return StreamSupport.stream( repository.findAll().spliterator(), false ).toList();
    }

    @Transactional(readOnly = true)
    public M getById( long id ) {
        return getById( id, () -> {
            throw new RuntimeException( "Object of class " + getModelClass() + " and id: " + id + " not found" );
        } );
    }

    public Class<M> getModelClass() {
        return (Class<M>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[ 0 ];
    }

    @Transactional(readOnly = true)
    public M getById( long id, Supplier<? extends M> supplier ) {
        return repository.findById( id ).orElseGet( supplier );
    }

    @Transactional
    public M save( M model ) {
        return repository.save( model );
    }

    @Transactional
    public M update( Long id, M updatedObject ) {
        M object = getById( id );
        updateData( object, updatedObject );
        return save( object );
    }

    protected abstract void updateData( M existingObject, M updatedObject) ;
//    {
//        existingObject.setName( updatedObject.getName() );
//        existingObject.setPassword( updatedObject.getPassword() );
//    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    @Transactional
    public void delete( long id ) {
        repository.deleteById( id );
    }

    @Transactional
    public void delete( M model ) {
        repository.delete( model );
    }
}
