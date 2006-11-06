(jde-set-project-name "ensj-core")
(setq jde-current-project-home (substring default-directory 
					  0 
					  (+ (search "ensj-core" default-directory ) (length "ensj-core") 1)))
(defun prepend-home (path) 
  (concat jde-current-project-home path)
  )

(setq jde-compile-option-directory (prepend-home "build/classes"))
(setq jde-compile-option-command-line-args "-target 1.4 -source 1.4")

(setq jde-gen-buffer-boilerplate (quote ("/*
    Copyright (C) 2001 EBI, GRL

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 */")))

(setq jde-sourcepath (concat jde-current-project-home "src"))

(setq jde-global-classpath (mapcar 'prepend-home '("."
						   "build/classes" 
						   "lib/java-getopt-1.0.9.jar" 
						   "lib/junit.jar" 
						   "lib/mysql-connector-java-3.1.8-bin.jar" 
						   "lib/p6spy.jar" 
						   "lib/mail.jar" 
						   "lib/activation.jar" 
						   "lib/looks-1.2.1.jar"
						   "lib/colt.jar"
						   )))

