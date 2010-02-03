/*******************************************************************************
 * Copyright (c) 2010, Barthelemy Dagenais All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package py4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import py4j.reflection.ReflectionEngine;

public class ReflectionCommand extends AbstractCommand {

	private final Logger logger = Logger.getLogger(ReflectionCommand.class
			.getName());

	public final static char GET_UNKNOWN_SUB_COMMAND_NAME = 'u';

	public final static char GET_MEMBER_SUB_COMMAND_NAME = 'm';
	
	public static final String REFLECTION_COMMAND_NAME = "r";

	protected ReflectionEngine rEngine;
	
	@Override
	public void init(Gateway gateway) {
		super.init(gateway);
		rEngine = gateway.getReflectionEngine();
	}

	@Override
	public void execute(String commandName, BufferedReader reader,
			BufferedWriter writer) throws Py4JException, IOException {
		char subCommand = reader.readLine().charAt(0);
		String returnCommand = null;

		if (subCommand == GET_UNKNOWN_SUB_COMMAND_NAME) {
			returnCommand = getUnknownMember(reader);
		} else {
			returnCommand = getMember(reader);
		}

		logger.info("Returning command: " + returnCommand);
		writer.write(returnCommand);
		writer.flush();
	}

	/**
	 * 1- Try fields.
	 * 2- If no static field, try methods.
	 * 3- If method and static, return method.
	 * 4- If method and not static, then class is impossible so return exception.
	 * 5- If no method, try class.
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private String getMember(BufferedReader reader) throws IOException {
		String fqn = reader.readLine();
		String member = reader.readLine();
		reader.readLine();
		String returnCommand = null;
		try {
			Class<?> clazz = Class.forName(fqn,false,this.getClass().getClassLoader());
			Field f = rEngine.getField(clazz, member);
			if (f != null && Modifier.isStatic(f.getModifiers())) {
				Object obj = rEngine.getFieldValue(null, f);
				ReturnObject rObject = gateway.getReturnObject(obj);
				returnCommand = Protocol.getOutputCommand(rObject);
			}
			
			if (returnCommand == null) {
				Method m = rEngine.getMethod(clazz, member);
				if (m != null) {
					if (Modifier.isStatic(m.getModifiers())) {
						returnCommand = Protocol.getMemberOutputCommand(Protocol.METHOD_TYPE);
					} else {
						returnCommand = Protocol.getOutputErrorCommand();
					}
				}
			}
			
			if (returnCommand == null) {
				Class<?> c = rEngine.getClass(clazz, member);
				if (c != null) {
					returnCommand = Protocol.getMemberOutputCommand(Protocol.CLASS_TYPE);
				} else {
					returnCommand = Protocol.getOutputErrorCommand();
				}
			}
		} catch(Exception e) {
			returnCommand = Protocol.getOutputErrorCommand();
		}
		
		return returnCommand;
	}

	private String getUnknownMember(BufferedReader reader) throws IOException {
		String fqn = reader.readLine();
		reader.readLine();
		String returnCommand = null;
		try {
			Class.forName(fqn,false,this.getClass().getClassLoader());
			returnCommand = Protocol.getMemberOutputCommand(Protocol.CLASS_TYPE);
		} catch (ClassNotFoundException e) {
			returnCommand = Protocol.getMemberOutputCommand(Protocol.PACKAGE_TYPE);
		} catch(Exception e) {
			returnCommand = Protocol.getOutputErrorCommand();
		}
		return returnCommand;
	}
	
}