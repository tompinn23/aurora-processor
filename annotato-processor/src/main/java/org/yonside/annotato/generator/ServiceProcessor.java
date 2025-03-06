package org.yonside.annotato.generator;

import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GenerateUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static org.yonside.annotato.generator.APContext.*;

@GenerateUtils
@GenerateAPContext
@SupportedAnnotationTypes({
        ServicePrism.PRISM_TYPE
})
public final class ServiceProcessor extends AbstractProcessor {

    private Elements elements;

    private Map<String, Set<String>> serviceImplementations = new HashMap<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        APContext.init(processingEnv);
        this.elements = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        APContext.setProjectModuleElement(annotations, roundEnv);

        final var processingOver = roundEnv.processingOver();

        maybeElements(roundEnv, ServicePrism.PRISM_TYPE).ifPresent(this::readServices);

        if(processingOver) {
            for(var kv : serviceImplementations.entrySet()) {
                try {
                    var file = createMetaInfServiceWriter(kv.getKey());
                    var writer = file.openWriter();
                    for(var line : kv.getValue()) {
                        writer.write(line);
                    }
                    writer.close();
                } catch (IOException e) {
                    logError("Failed to write services file: %s", e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }

        return false;
    }

    private List<TypeElement> getInterfaces(TypeElement element) {
        var ret = new ArrayList<TypeElement>();

        var specified = ServicePrism.getInstanceOn(element).value();
        var interfaces = element.getInterfaces();
        boolean superClass = element.getSuperclass().getKind() != TypeKind.NONE && !"java.lang.Object".equals(asTypeElement(element.getSuperclass()).getQualifiedName().toString());

        if(specified == null || specified.isEmpty()) {
            if(superClass ^ !interfaces.isEmpty()) {
                if(superClass) {
                    ret.add(asTypeElement(element.getSuperclass()));
                } else {
                    ret.add(asTypeElement(element.getInterfaces().get(0)));
                }
            } else {
                logError(element, "Service type was not provided and was unable to be inferred");
            }
        } else {
            for (var spi : specified) {
                if (interfaces.isEmpty() && !superClass || !superTypes(element).anyMatch(spi.toString()::equals)) {
                    logError(element, "Service Implementation does not extend %s", spi);
                } else if (spi instanceof DeclaredType) {
                    ret.add(asTypeElement(spi));
                } else {
                    logError(element, "Invalid type specified as service interface: %s", spi);
                }
            }
        }

        return ret;
    }

    private Stream<String> superTypes(TypeElement element) {
        return types().directSupertypes(element.asType()).stream()
                .filter(type -> !type.toString().contains("java.lang.Object"))
                .map(s -> (TypeElement)types().asElement(s))
                .flatMap(e -> Stream.concat(superTypes(e), Stream.of(e)))
                .map(Object::toString);
    }

    private void readServices(Set<? extends Element> serviceElements) {
        var serviceImpls = ElementFilter.typesIn(serviceElements);
        for(var element : serviceImpls) {
            var modifiers = element.getModifiers();

            if(!modifiers.contains(Modifier.PUBLIC) || element.getEnclosingElement().getKind() == ElementKind.CLASS && !modifiers.contains(Modifier.STATIC)) {
                logError(element, "Service Implementation must be a public class or a public static inner class");
            }

            if(ElementFilter.constructorsIn(element.getEnclosedElements()).stream()
                    .filter(e -> e.getParameters().isEmpty())
                    .filter(e -> e.getModifiers().contains(Modifier.PUBLIC))
                    .findAny().isEmpty()) {
                logError(element, "Service Implementation must have a public no-args constructor");
            }

            var services = getInterfaces(element);
            if(services.isEmpty()) {
                logError(element, "Service Implementation must have a service interface");
            }
            for(var service : services) {
                serviceImplementations.computeIfAbsent(elements.getBinaryName(service).toString(), k -> new HashSet<>()).add(elements.getBinaryName(element).toString());
            }

        }
    }

    FileObject createMetaInfServiceWriter(final String name) throws IOException {
        return APContext.filer().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + name);
    }

    // Optional because these annotations are not guaranteed to exist
    private static Optional<? extends Set<? extends Element>> maybeElements(RoundEnvironment round, String name) {
        return Optional.ofNullable(typeElement(name)).map(round::getElementsAnnotatedWith);
    }
}
