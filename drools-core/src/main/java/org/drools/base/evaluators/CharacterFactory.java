package org.drools.base.evaluators;

/*
 * Copyright 2005 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.drools.base.BaseEvaluator;
import org.drools.base.ValueType;
import org.drools.base.evaluators.IntegerFactory.IntegerMemberOfEvaluator;
import org.drools.base.evaluators.IntegerFactory.IntegerNotMemberOfEvaluator;
import org.drools.rule.VariableRestriction.LongVariableContextEntry;
import org.drools.rule.VariableRestriction.VariableContextEntry;
import org.drools.spi.Evaluator;
import org.drools.spi.Extractor;
import org.drools.spi.FieldValue;

public class CharacterFactory
    implements
    EvaluatorFactory {

    private static final long       serialVersionUID = -8006570416583057447L;
    private static EvaluatorFactory INSTANCE         = new CharacterFactory();

    private CharacterFactory() {

    }

    public static EvaluatorFactory getInstance() {
        if ( CharacterFactory.INSTANCE == null ) {
            CharacterFactory.INSTANCE = new CharacterFactory();
        }
        return CharacterFactory.INSTANCE;
    }

    public Evaluator getEvaluator(final Operator operator) {
        if ( operator == Operator.EQUAL ) {
            return CharacterEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.NOT_EQUAL ) {
            return CharacterNotEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.LESS ) {
            return CharacterLessEvaluator.INSTANCE;
        } else if ( operator == Operator.LESS_OR_EQUAL ) {
            return CharacterLessOrEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.GREATER ) {
            return CharacterGreaterEvaluator.INSTANCE;
        } else if ( operator == Operator.GREATER_OR_EQUAL ) {
            return CharacterGreaterOrEqualEvaluator.INSTANCE;
        } else if ( operator == Operator.MEMBEROF ) {
            return CharacterMemberOfEvaluator.INSTANCE;
        } else if ( operator == Operator.NOTMEMBEROF ) {
            return CharacterNotMemberOfEvaluator.INSTANCE;
        } else {
            throw new RuntimeException( "Operator '" + operator + "' does not exist for CharacterEvaluator" );
        }
    }

    static class CharacterEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 320;
        public final static Evaluator INSTANCE         = new CharacterEqualEvaluator();

        private CharacterEqualEvaluator() {
            super( ValueType.PCHAR_TYPE,
                   Operator.EQUAL );
        }

        public boolean evaluate(final Extractor extractor,
                                final Object object1,
                                final FieldValue object2) {
            if ( extractor.isNullValue( object1 ) ) {
                return object2.isNull();
            } else if ( object2.isNull() ) {
                return false;
            }
            
            return extractor.getCharValue( object1 ) == object2.getCharValue();
        }

        public boolean evaluateCachedRight(final VariableContextEntry context,
                                           final Object left) {
            if ( context.declaration.getExtractor().isNullValue( left ) ) {
                return context.isRightNull();
            } else if ( context.isRightNull() ) {
                return false;
            }
            
            return context.declaration.getExtractor().getCharValue( left ) == ((LongVariableContextEntry) context).right;
        }

        public boolean evaluateCachedLeft(final VariableContextEntry context,
                                          final Object right) {
            if ( context.extractor.isNullValue( right )) {
                return context.isLeftNull();
            } else if ( context.isLeftNull() ) {
                return false;
            }
            
            return ((LongVariableContextEntry) context).left == context.extractor.getCharValue( right );
        }

        public boolean evaluate(final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2,
                                final Object object2) {
            if (extractor1.isNullValue( object1 )) {
                return extractor2.isNullValue( object2 );
            } else if (extractor2.isNullValue( object2 )) {
                return false;
            }
            
            return extractor1.getCharValue( object1 ) == extractor2.getCharValue( object2 );
        }

        public String toString() {
            return "Character ==";
        }
    }

    static class CharacterNotEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 320;
        public final static Evaluator INSTANCE         = new CharacterNotEqualEvaluator();

        private CharacterNotEqualEvaluator() {
            super( ValueType.PCHAR_TYPE,
                   Operator.NOT_EQUAL );
        }

        public boolean evaluate(final Extractor extractor,
                                final Object object1,
                                final FieldValue object2) {
            if ( extractor.isNullValue( object1 ) ) {
                return !object2.isNull();
            } else if ( object2.isNull() ) {
                return true;
            }
            
            return extractor.getCharValue( object1 ) != object2.getCharValue();
        }

        public boolean evaluateCachedRight(final VariableContextEntry context,
                                           final Object left) {
            if ( context.declaration.getExtractor().isNullValue( left ) ) {
                return !context.isRightNull();
            } else if ( context.isRightNull() ) {
                return true;
            }
            
            return context.declaration.getExtractor().getCharValue( left ) != ((LongVariableContextEntry) context).right;
        }

        public boolean evaluateCachedLeft(final VariableContextEntry context,
                                          final Object right) {
            if ( context.extractor.isNullValue( right ) ) {
                return !context.isLeftNull();
            } else if ( context.isLeftNull() ) {
                return true;
            }
            
            return ((LongVariableContextEntry) context).left != context.extractor.getCharValue( right );
        }

        public boolean evaluate(final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2,
                                final Object object2) {
            if (extractor1.isNullValue( object1 )) {
                return !extractor2.isNullValue( object2 );
            } else if (extractor2.isNullValue( object2 )) {
                return true;
            }
            
            return extractor1.getCharValue( object1 ) != extractor2.getCharValue( object2 );
        }

        public String toString() {
            return "Character !=";
        }
    }

    static class CharacterLessEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 320;
        public final static Evaluator INSTANCE         = new CharacterLessEvaluator();

        private CharacterLessEvaluator() {
            super( ValueType.PCHAR_TYPE,
                   Operator.LESS );
        }

        public boolean evaluate(final Extractor extractor,
                                final Object object1,
                                final FieldValue object2) {
            return extractor.getCharValue( object1 ) < object2.getCharValue();
        }

        public boolean evaluateCachedRight(final VariableContextEntry context,
                                           final Object left) {
            return ((LongVariableContextEntry) context).right < context.declaration.getExtractor().getCharValue( left );
        }

        public boolean evaluateCachedLeft(final VariableContextEntry context,
                                          final Object right) {
            return context.extractor.getCharValue( right ) < ((LongVariableContextEntry) context).left;
        }

        public boolean evaluate(final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2,
                                final Object object2) {
            return extractor1.getCharValue( object1 ) < extractor2.getCharValue( object2 );
        }

        public String toString() {
            return "Character <";
        }
    }

    static class CharacterLessOrEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 320;
        public final static Evaluator INSTANCE         = new CharacterLessOrEqualEvaluator();

        private CharacterLessOrEqualEvaluator() {
            super( ValueType.PCHAR_TYPE,
                   Operator.LESS_OR_EQUAL );
        }

        public boolean evaluate(final Extractor extractor,
                                final Object object1,
                                final FieldValue object2) {
            return extractor.getCharValue( object1 ) <= object2.getCharValue();
        }

        public boolean evaluateCachedRight(final VariableContextEntry context,
                                           final Object left) {
            return ((LongVariableContextEntry) context).right <= context.declaration.getExtractor().getCharValue( left );
        }

        public boolean evaluateCachedLeft(final VariableContextEntry context,
                                          final Object right) {
            return context.extractor.getCharValue( right ) <= ((LongVariableContextEntry) context).left;
        }

        public boolean evaluate(final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2,
                                final Object object2) {
            return extractor1.getCharValue( object1 ) <= extractor2.getCharValue( object2 );
        }

        public String toString() {
            return "Character <=";
        }
    }

    static class CharacterGreaterEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long     serialVersionUID = 320;
        public final static Evaluator INSTANCE         = new CharacterGreaterEvaluator();

        private CharacterGreaterEvaluator() {
            super( ValueType.PCHAR_TYPE,
                   Operator.GREATER );
        }

        public boolean evaluate(final Extractor extractor,
                                final Object object1,
                                final FieldValue object2) {
            return extractor.getCharValue( object1 ) > object2.getCharValue();
        }

        public boolean evaluateCachedRight(final VariableContextEntry context,
                                           final Object left) {
            return ((LongVariableContextEntry) context).right > context.declaration.getExtractor().getCharValue( left );
        }

        public boolean evaluateCachedLeft(final VariableContextEntry context,
                                          final Object right) {
            return context.extractor.getCharValue( right ) > ((LongVariableContextEntry) context).left;
        }

        public boolean evaluate(final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2,
                                final Object object2) {
            return extractor1.getCharValue( object1 ) > extractor2.getCharValue( object2 );
        }

        public String toString() {
            return "Character >";
        }
    }

    static class CharacterGreaterOrEqualEvaluator extends BaseEvaluator {
        /**
         * 
         */
        private static final long      serialVersionUID = 320;
        private final static Evaluator INSTANCE         = new CharacterGreaterOrEqualEvaluator();

        private CharacterGreaterOrEqualEvaluator() {
            super( ValueType.PCHAR_TYPE,
                   Operator.GREATER_OR_EQUAL );
        }

        public boolean evaluate(final Extractor extractor,
                                final Object object1,
                                final FieldValue object2) {
            return extractor.getCharValue( object1 ) >= object2.getCharValue();
        }

        public boolean evaluateCachedRight(final VariableContextEntry context,
                                           final Object left) {
            return ((LongVariableContextEntry) context).right >= context.declaration.getExtractor().getCharValue( left );
        }

        public boolean evaluateCachedLeft(final VariableContextEntry context,
                                          final Object right) {
            return context.extractor.getCharValue( right ) >= ((LongVariableContextEntry) context).left;
        }

        public boolean evaluate(final Extractor extractor1,
                                final Object object1,
                                final Extractor extractor2,
                                final Object object2) {
            return extractor1.getCharValue( object1 ) >= extractor2.getCharValue( object2 );
        }

        public String toString() {
            return "Character >=";
        }
    }

    static class CharacterMemberOfEvaluator extends BaseMemberOfEvaluator {

        private static final long     serialVersionUID = 320;
        public final static Evaluator INSTANCE         = new CharacterMemberOfEvaluator();

        private CharacterMemberOfEvaluator() {
            super( ValueType.PCHAR_TYPE,
                   Operator.MEMBEROF );
        }

        public String toString() {
            return "Character memberOf";
        }
    }

    static class CharacterNotMemberOfEvaluator extends BaseNotMemberOfEvaluator {

        private static final long     serialVersionUID = 320;
        public final static Evaluator INSTANCE         = new CharacterNotMemberOfEvaluator();

        private CharacterNotMemberOfEvaluator() {
            super( ValueType.PCHAR_TYPE,
                   Operator.NOTMEMBEROF );
        }

        public String toString() {
            return "Character not memberOf";
        }
    }
    
}