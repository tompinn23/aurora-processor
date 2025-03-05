package uk.org.yonside.annotato.generator;

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
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static uk.org.yonside.annotato.generator.APContext.logError;
import static uk.org.yonside.annotato.generator.APContext.typeElement;

@GenerateUtils
@GenerateAPContext
@SupportedAnnotationTypes({
        ServicePrism.PRISM_TYPE
})
public final class Processor extends AbstractProcessor {

    private Elements elements;

    private Map<String, String> serviceImplementations = new HashMap<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        APContext.init(processingEnv);
        this.elements = processingEnv.getElementUtils();


        try {
            final var uri = processingEnv.getFiler()
                    .createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/spi-service-locator")
                    .toUri();

            var path = Path.of(uri).getParent();

        } catch (IOException e) {
            // ignore
        }
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
                    writer.write(kv.getValue());
                    writer.close();
                } catch (IOException e) {
                    logError("Failed to write services file: %s", e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }

        return false;
    }

    private void readServices(Set<? extends Element> serviceElements) {
        var serviceImpls = ElementFilter.typesIn(serviceElements);
        for(var element : serviceImpls) {
            if(element.getKind() == ElementKind.INTERFACE) {
                continue;
            }

            if(!element.getModifiers().contains(Modifier.FINAL)) {
                throw new IllegalStateException("Service class '" + element.getSimpleName() + "' is not final");
            }

            if(element.getSuperclass().getKind() != TypeKind.NONE && elements.getTypeElement("java.lang.Object").equals(element.getSuperclass())) {
                throw new IllegalStateException("Service class '" + element.getSimpleName() + "' has a superclass");
            }

            for(var face : element.getInterfaces()) {
                if(serviceImplementations.containsKey(face)) {
                    throw new IllegalStateException("Service '" + face.toString() + "' has multiple implementations");
                }
                serviceImplementations.put(face.toString(), element.getQualifiedName().toString());
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
