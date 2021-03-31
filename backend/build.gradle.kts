import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.moowork.gradle.node.npm.NpmTask

plugins {
	id("org.springframework.boot") version "2.5.0-SNAPSHOT"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.4.31"
	kotlin("plugin.spring") version "1.4.31"
	id("com.github.node-gradle.node") version "2.2.2"
}

group = "lmgillaspie"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.register<NpmTask>("appNpmInstall") {
	description = "Installs all dependencies from package.json"
	workingDir = file("../frontend")
	args = listOf("install")
}

tasks.register<NpmTask>("appNpmBuild") {
	dependsOn("appNpmInstall")
	description = "Builds project"
	workingDir = file("../frontend")
	args = listOf("run", "build")
}

tasks.register<Copy>("copyWebApp") {
	dependsOn("appNpmBuild")
	description = "Copies built project to where it will be served"
	from("../frontend/build")
	into("build/resources/main/static/.")
}

node {
	download = true
	version = "12.13.1"
	npmVersion = "6.12.1"
	// Set the work directory for unpacking node
	workDir = file("../../frontend/nodejs")
	// Set the work directory for NPM
	npmWorkDir = file("../../frontend/npm")
}

tasks.withType<KotlinCompile> {
	// So that all the tasks run with ./gradlew build
	dependsOn("copyWebApp")
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
