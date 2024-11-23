# Stage 1: Build the Angular application
FROM node:20.9.0 AS build

# Set the working directory
WORKDIR /app

# Install Angular CLI
RUN npm install -g @angular/cli@18

# Copy package.json and package-lock.json to install dependencies
COPY package.json package-lock.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code
COPY . .

# Build the Angular application in production mode
RUN ng build --prod

# Temporary step to list the contents of the build directory
RUN ls -al /app/dist

# Stage 2: Serve the Angular application with Nginx
FROM nginx:alpine

# Copy the build output to the Nginx HTML directory
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration file
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start Nginx server
CMD ["nginx", "-g", "daemon off;"]
