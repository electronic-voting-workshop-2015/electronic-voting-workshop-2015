ENV['RACK_ENV'] = 'test'
require 'minitest/autorun'
require 'rack/test'
require 'app/app'

#Configuration.verify_signature = false

module TestHelper
    def randomString( length: 10 )
        alaphabet = 'abcdefghijklmnopqrtst0123456789!@#$%^&*'.split( '' )
        array = alaphabet.sample( length )
        array.join( '' )
    end

    def setup
         ActiveRecord::Base.subclasses.each do |model|
             begin
                 model.delete_all
             rescue ActiveRecord::StatementInvalid => e
                 puts "skipping #{model}, it's probably a model without a table (exception was #{e})"
             end
         end
    end

    def randomInteger
        (Random.rand * 10000000).to_i
    end
end
