ClassSupplier
===========

ClassSupplier is the Eclipse plug-in allowing to create classes programmatically, making them accessible to client bundle.  

It actually generates source code from the provided model, exports binary, installs it into its own runtime, and then loads classes making them available to client code through model's reflective API.  

Here is how you can use it:  

    // Design a blueprint model - dynamic EMF EPackage
    EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
    ePackage.setName("library");
    ePackage.setNsPrefix("library");
    ePackage.setNsURI("http://library/1.0");
    EClass eClass = EcoreFactory.eINSTANCE.createEClass();
    eClass.setName("Book");
    EAttribute eAttr = EcoreFactory.eINSTANCE.createEAttribute();
    eAttr.setName("pages");
    eAttr.setEType(EcorePackage.Literals.EINT);
    eClass.getEStructuralFeatures().add(eAttr);
    ePackage.getEClassifiers().add(eClass);

    // Acquire ClassSupplier's OSGi service
    BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass())
                                .getBundleContext();
    ServiceReference<?> serviceReference = bundleContext
                              .getServiceReference(ClassPlant.class);
    ClassPlant classPlant = (ClassPlant) bundleContext
                              .getService(serviceReference);

    // Produce result by combining them
    EPackage jPackage = classPlant.produce(ePackage);
    
    ...
    // Use the generated model at runtime
    EClass jClass = (EClass) jPackage.getEClassifier(eClass.getName());
    EObject jObject = jPackage.getEFactoryInstance().create(jClass); 
    int pages = 500;
    EStructuralFeature jAttr = jClass.getEStructuralFeature(eAttr
        .getName());
    jObject.eSet(jAttr, pages);
    assertEquals(pages, jObject.eGet(jAttr));
    assertEquals(eClass.getName(), jObject.getClass().getSimpleName());  
        
There is more [code](/tests/org.enterprisedomain.tests/src/org/enterprisedomain/tests/TestEnterpriseDomain.java), where you can specify a method body and call the method, or customize the generation templates. 


Feedback
---------
If you have anything to suggest, please feel free to file a [bug at GitHub](https://github.com/kyrillzotkin/ClassSupplier/issues). 

