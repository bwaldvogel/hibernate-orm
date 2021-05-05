/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping;

import java.util.function.IntFunction;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;

/**
 * Descriptor for foreign-keys
 */
public interface ForeignKeyDescriptor extends VirtualModelPart {
	String PART_NAME = "{fk}";
	String TARGET_PART_NAME = "{fk-target}";

	String getKeyTable();

	String getTargetTable();

	ModelPart getKeyPart();

	ModelPart getTargetPart();

	/**
	 * Create a DomainResult for the referring-side of the fk
	 */
	DomainResult<?> createKeyDomainResult(
			NavigablePath collectionPath,
			TableGroup tableGroup,
			DomainResultCreationState creationState);

	/**
	 * Create a DomainResult for the target-side of the fk
	 */
	DomainResult<?> createTargetDomainResult(
			NavigablePath collectionPath,
			TableGroup tableGroup,
			DomainResultCreationState creationState);

	DomainResult createCollectionFetchDomainResult(
			NavigablePath collectionPath,
			TableGroup tableGroup,
			DomainResultCreationState creationState);

	DomainResult createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			DomainResultCreationState creationState);

	DomainResult createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			boolean isKeyReferringSide,
			DomainResultCreationState creationState);

	Predicate generateJoinPredicate(
			TableGroup lhs,
			TableGroup tableGroup,
			SqlAstJoinType sqlAstJoinType,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext);

	Predicate generateJoinPredicate(
			TableReference lhs,
			TableReference rhs,
			SqlAstJoinType sqlAstJoinType,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext);

	@Override
	default String getPartName() {
		return PART_NAME;
	}

	/**
	 * Visits the FK "referring" columns
	 */
	@Override
	default int forEachSelectable(int offset, SelectableConsumer consumer) {
		return visitKeySelectables( offset, consumer );
	}

	Object getAssociationKeyFromTarget(Object targetObject, SharedSessionContractImplementor session);

	int visitKeySelectables(int offset, SelectableConsumer consumer);

	default int visitKeySelectables(SelectableConsumer consumer)  {
		return visitKeySelectables( 0, consumer );
	}

	int visitTargetSelectables(int offset, SelectableConsumer consumer);

	default int visitTargetSelectables(SelectableConsumer consumer) {
		return visitTargetSelectables( 0, consumer );
	}

	/**
	 * Return a copy of this foreign key descriptor with the selectable mappings as provided by the given accessor.
	 */
	ForeignKeyDescriptor withKeySelectionMapping(
			IntFunction<SelectableMapping> selectableMappingAccess,
			MappingModelCreationProcess creationProcess);

	AssociationKey getAssociationKey();
}