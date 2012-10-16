<project>

       <modelVersion>4.0.0</modelVersion>
         <groupId>$groupId$</groupId>
        <artifactId>$artifactId$</artifactId>
        <name>$NAME$-profile</name>
        <packaging>pom</packaging>

        <parent>
            <groupId>$groupId$</groupId>
            <artifactId>$artifactId_parent$</artifactId>
            <version>$version_parent$</version>
       </parent>

    <profiles>
        <profile>
            <id>nix32</id>
            <activation>
                <os>
                    <family>unix</family>
                    <name>Linux</name>
                    <arch>i386</arch>
                </os>
            </activation>
            <modules>
                <module>nix32</module>
            </modules>
        </profile>

        <profile>
            <id>nix64</id>
            <activation>
                <os>
                    <family>unix</family>
                    <name>Linux</name>
                    <arch>x64</arch>
                </os>
            </activation>
            <modules>
                <module>nix64</module>
            </modules>
        </profile>
        <profile>
            <id>osx</id>
            <activation>
                <os>
                    <family>mac</family>
                </os>
            </activation>

            <modules>
                <module>osx</module>
            </modules>
        </profile>
            <profile>
                       <id>arm</id>
                       <activation>
                           <os>
                               <family>unix</family>
                               <name>Linux</name>
                               <arch>arm</arch>
                           </os>
                       </activation>
                       <modules>
                           <module>arm</module>
                       </modules>
                   </profile>
    </profiles>

        <distributionManagement>
            <repository>
                <id>maven2.kevoree.release</id>
                <url>http://maven.kevoree.org/archiva/repository/release/</url>
            </repository>
            <snapshotRepository>
                <id>maven2.kevoree.snapshots</id>
                <url>http://maven.kevoree.org/archiva/repository/snapshots/</url>
            </snapshotRepository>
        </distributionManagement>

        <repositories>
            <repository>
                <id>kevoree-libs-release-local</id>
                <url>http://maven.kevoree.org/release</url>
            </repository>
            <repository>
                <id>kevoree-snapshots</id>
                <url>http://maven.kevoree.org/snapshots</url>
            </repository>
        </repositories>

        <pluginRepositories>
            <pluginRepository>
                <id>plugin.kevoree-github</id>
                <url>http://maven.kevoree.org/release</url>
            </pluginRepository>
            <pluginRepository>
                <id>plugin.kevoree-snapshots</id>
                <url>http://maven.kevoree.org/snapshots</url>
            </pluginRepository>
        </pluginRepositories>


</project>
