module Configuration
    @verify_signature = ! ENV.include?( 'BB_INSECURE' )

    def self.verify_signature=( value )
        @verify_signature = value
    end

    def self.verify_signature
        @verify_signature
    end
end
