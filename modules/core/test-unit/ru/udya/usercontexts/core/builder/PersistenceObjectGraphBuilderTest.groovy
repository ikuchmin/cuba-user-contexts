package ru.udya.usercontexts.core.builder

import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.Metadata
import ru.udya.usercontexts.builder.PersistenceObjectGraphBuilder
import ru.udya.usercontexts.core.builder.subpackage.E
import spock.lang.Specification

@SuppressWarnings("GroovyAssignabilityCheck")
class PersistenceObjectGraphBuilderTest extends Specification {

    PersistenceObjectGraphBuilder delegate

    Metadata predefinedMetadata
    DataManager predefinedDataManager

    void setup() {
        predefinedMetadata = Mock(Metadata) {
            create(_) >> { arg -> arg[0].newInstance() }
        }
        predefinedDataManager = Mock(DataManager)

        delegate = PersistenceObjectGraphBuilder.newInstance(predefinedMetadata, predefinedDataManager)
        delegate.classLoader = this.class.classLoader
        delegate.classNameResolver = "ru.rsmu.nmifo.builder"
    }

    def "create the entity (fields as method args)"() {
        given:
        def expected = new A()
        expected.with { id = UUID.randomUUID() }

        when:
        A a = delegate.A(id: expected.id)

        then:
        a == expected
        1 * predefinedDataManager.commit(_ as CommitContext)
    }

    def "create the entity (fields as closure assignment)"() {
        given:
        def expected = new A()
        expected.with { id = UUID.randomUUID() }

        when:
        A a = delegate.A {
            $$.id = expected.id
        }

        then:
        a == expected
        1 * predefinedDataManager.commit(_ as CommitContext)
    }

    def "create the entity with oneToMany dependencies (fields as method args)"() {
        given:
        def expectedB1 = new B()
        expectedB1.with { id = UUID.randomUUID() }

        def expectedB2 = new B()
        expectedB2.with { id = UUID.randomUUID() }

        def expectedA = new A()
        expectedA.with {
            id = UUID.randomUUID()
            bs = [expectedB1, expectedB2]
        }
        expectedB1.a = expectedA
        expectedB2.a = expectedA

        when:
        A a = delegate.A(id: expectedA.id) {
            $$.bs = [B(id: expectedB1.id),
                     B(id: expectedB2.id)]
        }

        then:
        a == expectedA
        a.bs == [expectedB1, expectedB2]
        a.bs[0].a == expectedA
        a.bs[1].a == expectedA

        1 * predefinedDataManager.commit(_ as CommitContext)
    }

    def "create the entity with oneToMany dependencies (fields as closure assignment)"() {
        given:
        def expectedB1 = new B()
        expectedB1.with { id = UUID.randomUUID() }

        def expectedB2 = new B()
        expectedB2.with { id = UUID.randomUUID() }

        def expectedA = new A()
        expectedA.with {
            id = UUID.randomUUID()
            bs = [expectedB1, expectedB2]
        }
        expectedB1.a = expectedA
        expectedB2.a = expectedA

        when:
        A a = delegate.A {
            $$.id = expectedA.id
            $$.bs = [B { $$.id = expectedB1.id },
                     B { $$.id = expectedB2.id }]
        }

        then:
        a == expectedA
        a.bs == [expectedB1, expectedB2]
        a.bs[0].a == expectedA
        a.bs[1].a == expectedA

        1 * predefinedDataManager.commit(_ as CommitContext)
    }

    def "create the entity with deep dependencies (fields as method args)"() {
        given:
        def expectedC1 = new C()
        expectedC1.with { id = UUID.randomUUID()}

        def expectedC2 = new C()
        expectedC2.with { id = UUID.randomUUID()}

        def expectedC3 = new C()
        expectedC2.with { id = UUID.randomUUID()}

        def expectedC4 = new C()
        expectedC2.with { id = UUID.randomUUID()}

        def expectedB1 = new B()
        expectedB1.with {
            id = UUID.randomUUID()
            cs = [expectedC1, expectedC2]
        }

        def expectedB2 = new B()
        expectedB2.with {
            id = UUID.randomUUID()
            cs = [expectedC3, expectedC4]
        }

        def expectedA = new A()
        expectedA.with {
            id = UUID.randomUUID()
            bs = [expectedB1, expectedB2]
        }

        expectedB1.a = expectedA
        expectedB2.a = expectedA
        expectedC1.b = expectedB1
        expectedC2.b = expectedB1
        expectedC3.b = expectedB2
        expectedC4.b = expectedB2

        when:
        A a = delegate.A(id: expectedA.id) {
            $$.bs = [B(id: expectedB1.id) {
                         $$.cs = [C(id: expectedC1.id), C(id: expectedC2.id)]},
                     B(id: expectedB2.id) {
                         $$.cs = [C(id: expectedC3.id), C(id: expectedC4.id)]}]
        }

        then:
        a == expectedA
        a.bs == [expectedB1, expectedB2]
        a.bs[0].a == expectedA
        a.bs[0].cs == [expectedC1, expectedC2]
        a.bs[0].cs[0].b == a.bs[0]
        a.bs[0].cs[1].b == a.bs[0]
        a.bs[1].a == expectedA
        a.bs[1].cs == [expectedC3, expectedC4]
        a.bs[1].cs[0].b == a.bs[1]
        a.bs[1].cs[1].b == a.bs[1]

        1 * predefinedDataManager.commit(_ as CommitContext)
    }

    def "create the entity with deep dependencies (fields as closure assignment)"() {
        given:
        def expectedC1 = new C()
        expectedC1.with { id = UUID.randomUUID()}

        def expectedC2 = new C()
        expectedC2.with { id = UUID.randomUUID()}

        def expectedC3 = new C()
        expectedC2.with { id = UUID.randomUUID()}

        def expectedC4 = new C()
        expectedC2.with { id = UUID.randomUUID()}

        def expectedB1 = new B()
        expectedB1.with {
            id = UUID.randomUUID()
            cs = [expectedC1, expectedC2]
        }

        def expectedB2 = new B()
        expectedB2.with {
            id = UUID.randomUUID()
            cs = [expectedC3, expectedC4]
        }

        def expectedA = new A()
        expectedA.with {
            id = UUID.randomUUID()
            bs = [expectedB1, expectedB2]
        }

        expectedB1.a = expectedA
        expectedB2.a = expectedA
        expectedC1.b = expectedB1
        expectedC2.b = expectedB1
        expectedC3.b = expectedB2
        expectedC4.b = expectedB2

        when:
        A a = delegate.A {
            $$.id = expectedA.id
            $$.bs = [B { $$.id = expectedB1.id
                         $$.cs = [C { $$.id = expectedC1.id },
                                  C { $$.id = expectedC2.id }]},
                     B { $$.id = expectedB2.id
                         $$.cs = [C { $$.id = expectedC3.id },
                                  C { $$.id = expectedC4.id }]}] }

        then:
        a == expectedA
        a.bs == [expectedB1, expectedB2]
        a.bs[0].a == expectedA
        a.bs[0].cs == [expectedC1, expectedC2]
        a.bs[0].cs[0].b == a.bs[0]
        a.bs[0].cs[1].b == a.bs[0]
        a.bs[1].a == expectedA
        a.bs[1].cs == [expectedC3, expectedC4]
        a.bs[1].cs[0].b == a.bs[1]
        a.bs[1].cs[1].b == a.bs[1]

        1 * predefinedDataManager.commit(_ as CommitContext)
    }

    def "explicitly defined parent"() {
        given:
        def expectedC1 = new C()
        expectedC1.with { id = UUID.randomUUID()}

        def expectedD1 = new D()
        expectedD1.with { id = UUID.randomUUID()}

        def expectedD2 = new D()
        expectedD2.with { id = UUID.randomUUID()}

        when:
        C c = delegate.C(id: expectedC1.id ) {
            $$.ds = [D(id: expectedD1.id) { $$.parentC = parent },
                     D(id: expectedD2.id) { $$.parentC = parent }]
        }

        then:
        c == expectedC1
        c.ds == [expectedD1, expectedD2]
        c.ds[0].parentC == c
        c.ds[1].parentC == c

        1 * predefinedDataManager.commit(_ as CommitContext)
    }

    def "create the entity with non trivial dependencies"() {
        given:
        def expectedD1 = new D()
        expectedD1.with { id = UUID.randomUUID()}

        def expectedD2 = new D()
        expectedD2.with { id = UUID.randomUUID()}

        def expectedC1 = new C()
        expectedC1.with { id = UUID.randomUUID(); ds = [expectedD1] }

        def expectedC2 = new C()
        expectedC2.with { id = UUID.randomUUID()}

        def expectedC3 = new C()
        expectedC2.with { id = UUID.randomUUID(); ds = [expectedD2] }

        def expectedC4 = new C()
        expectedC2.with { id = UUID.randomUUID()}

        def expectedB1 = new B()
        expectedB1.with {
            id = UUID.randomUUID()
            cs = [expectedC1, expectedC2]
        }

        def expectedB2 = new B()
        expectedB2.with {
            id = UUID.randomUUID()
            cs = [expectedC3, expectedC4]
        }

        def expectedA = new A()
        expectedA.with {
            id = UUID.randomUUID()
            bs = [expectedB1, expectedB2]
        }

        expectedB1.a = expectedA
        expectedB2.a = expectedA
        expectedC1.b = expectedB1
        expectedC2.b = expectedB1
        expectedC3.b = expectedB2
        expectedC4.b = expectedB2
        expectedD1.parentC = expectedC1
        expectedD1.bs = [expectedB2]
        expectedD2.parentC = expectedC3
        expectedD2.bs = [expectedB1, expectedB2]

        when:
        A a = delegate.A(id: expectedA.id) {
            $$.bs = [B(id: expectedB1.id, graphId: "b1") {
                         $$.cs = [C(id: expectedC1.id) {
                                      $$.ds = [D(id: expectedD1.id) {
                                                   $$.parentC = parent
                                                   $$.bs = [B(refId: "b1")]
                                               }]},
                                  C(id: expectedC2.id)]},
                     B(id: expectedB2.id, graphId: "b2") {
                         $$.cs = [C(id: expectedC3.id) {
                                      $$.ds = [D(id: expectedD1.id) {
                                                   $$.parentC = parent
                                                   $$.bs = [B(refId: "b1"),
                                                            B(refId: "b2")]
                                      }]},
                                  C(id: expectedC4.id)]}]
        }

        then:
        a == expectedA
        a.bs == [expectedB1, expectedB2]
        a.bs[0].a == expectedA
        a.bs[0].cs == [expectedC1, expectedC2]
        a.bs[0].cs[0].b == a.bs[0]
        a.bs[0].cs[0].ds == [expectedD1]
        a.bs[0].cs[0].ds[0].parentC == a.bs[0].cs[0]
        a.bs[0].cs[0].ds[0].bs == [expectedB1]
        a.bs[0].cs[1].b == a.bs[0]
        a.bs[1].a == expectedA
        a.bs[1].cs == [expectedC3, expectedC4]
        a.bs[1].cs[0].b == a.bs[1]
        a.bs[1].cs[0].ds == [expectedD1]
        a.bs[1].cs[0].ds[0].parentC == a.bs[1].cs[0]
        a.bs[1].cs[0].ds[0].bs == [expectedB1, expectedB2]
        a.bs[1].cs[1].b == a.bs[1]

        1 * predefinedDataManager.commit(_ as CommitContext)
    }

        def '''user cannot use graphId related with entity described below.
 Graph is interpreted, not compiled and run after all.'''() {
        given:
        def expectedD1 = new D()
        expectedD1.with { id = UUID.randomUUID()}

        def expectedD2 = new D()
        expectedD2.with { id = UUID.randomUUID()}

        def expectedC1 = new C()
        expectedC1.with { id = UUID.randomUUID(); ds = [expectedD1] }

        def expectedC2 = new C()
        expectedC2.with { id = UUID.randomUUID()}

        def expectedC3 = new C()
        expectedC2.with { id = UUID.randomUUID(); ds = [expectedD2] }

        def expectedC4 = new C()
        expectedC2.with { id = UUID.randomUUID()}

        def expectedB1 = new B()
        expectedB1.with {
            id = UUID.randomUUID()
            cs = [expectedC1, expectedC2]
        }

        def expectedB2 = new B()
        expectedB2.with {
            id = UUID.randomUUID()
            cs = [expectedC3, expectedC4]
        }

        def expectedA = new A()
        expectedA.with {
            id = UUID.randomUUID()
            bs = [expectedB1, expectedB2]
        }

        expectedB1.a = expectedA
        expectedB2.a = expectedA
        expectedC1.b = expectedB1
        expectedC2.b = expectedB1
        expectedC3.b = expectedB2
        expectedC4.b = expectedB2
        expectedD1.parentC = expectedC1
        expectedD1.bs = [expectedB2, null]
        expectedD2.parentC = expectedC3
        expectedD2.bs = [expectedB1, expectedB2]

        when:
        A a = delegate.A(id: expectedA.id) {
            $$.bs = [B(id: expectedB1.id, graphId: "b1") {
                         $$.cs = [C(id: expectedC1.id) {
                                      $$.ds = [D(id: expectedD1.id) {
                                                   $$.parentC = parent
                                                   $$.bs = [B(refId: "b1"),
                                                            B(refId: "b2")]
                                               }]},
                                  C(id: expectedC2.id)]},
                     B(id: expectedB2.id, graphId: "b2") {
                         $$.cs = [C(id: expectedC3.id) {
                                      $$.ds = [D(id: expectedD1.id) {
                                                   $$.parentC = parent
                                                   $$.bs = [B(refId: "b1"),
                                                            B(refId: "b2")]
                                      }]},
                                  C(id: expectedC4.id)]}]
        }

        then:
        a == expectedA
        a.bs == [expectedB1, expectedB2]
        a.bs[0].a == expectedA
        a.bs[0].cs == [expectedC1, expectedC2]
        a.bs[0].cs[0].b == a.bs[0]
        a.bs[0].cs[0].ds == [expectedD1]
        a.bs[0].cs[0].ds[0].parentC == a.bs[0].cs[0]
        a.bs[0].cs[0].ds[0].bs == [expectedB1, null]
        a.bs[0].cs[1].b == a.bs[0]
        a.bs[1].a == expectedA
        a.bs[1].cs == [expectedC3, expectedC4]
        a.bs[1].cs[0].b == a.bs[1]
        a.bs[1].cs[0].ds == [expectedD1]
        a.bs[1].cs[0].ds[0].parentC == a.bs[1].cs[0]
        a.bs[1].cs[0].ds[0].bs == [expectedB1, expectedB2]
        a.bs[1].cs[1].b == a.bs[1]

        1 * predefinedDataManager.commit(_ as CommitContext)
    }

    def "ClassNameResover supports list of packages for resolving class objects"() {
        given:
        delegate.classNameResolver = ["ru.rsmu.nmifo.builder", "ru.rsmu.nmifo.builder.subpackage"]

        def expectedE = new E()
        expectedE.with { id = UUID.randomUUID() }

        def expectedA = new A()
        expectedA.with { id = UUID.randomUUID() }

        when:
        E e = delegate.E(id: expectedE.id) {
            $$.a = A(id: expectedA.id)
        }

        then:
        e == expectedE
        e.a == expectedA

        1 * predefinedDataManager.commit(_ as CommitContext)

    }
}
